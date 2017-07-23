package node2;

import lombok.ToString;
import node2.in_out.*;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.*;
import static node2.in_out.Registry.emptyRegistry;

public class Gossip2 {
    public static final int PORT_START = 2000;
    private List<Server> servers;
    private Selector selector;
    private RegistryMapping<MessageType<Serializable>, Serializable> typeMapping;

    public static void main(String[] args) throws IOException {
        new Gossip2().run();
    }

    private void run() throws IOException {
        selector = Selector.open();

        typeMapping = emptyRegistry()
                .register(0x0, PingMessage.TYPE, PingMessage::new)
                .register(0x1, PongMessage.TYPE, PongMessage::new)
                .register(0x2, RandomWalkMessage.TYPE, RandomWalkMessage::new)
                .mapping(MessageType.class);

        servers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            servers.add(createServer(selector, i + PORT_START));
        }

        for (int i = 0; i < servers.size(); i++) {
            for (int j = i + 1; j < servers.size(); j++) {
                connect(servers.get(i),
                        selector,
                        servers.get(i).port,
                        servers.get(j).port);
            }
        }

        while (true) {
            selector.select();

            Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();

            while (keyIt.hasNext()) {
                SelectionKey key = keyIt.next();
                keyIt.remove();

                if (!key.isValid()) {
                    continue;
                }

                processKey(key);
            }
        }
    }

    private void processKey(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            handleConnect(key);
        } else if (key.isAcceptable()) {
            handleAccept(key);
        } else if (key.isWritable()) {
            handleWrite(key);
        } else if (key.isReadable()) {
            handleRead(key);
        }
    }

    Random rnd = new Random();
    AtomicInteger cnt = new AtomicInteger();

    Histogram hist = new Histogram(3);

    private void handleRead(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();

        Serializable received;

        while ((received = connection.receive()) != null) {
            if (received instanceof PingMessage) {
                PingMessage ping = (PingMessage) received;
                connection.portFrom = ping.getPort();

                PongMessage pong = new PongMessage();
                pong.port = ping.port;
                connection.send(pong);
            } else if (received instanceof PongMessage) {
                RandomWalkMessage rwm = new RandomWalkMessage();
                rwm.setHops(30000);
                rwm.setT(System.nanoTime());
                connection.send(rwm);
            } else if (received instanceof RandomWalkMessage) {
                Connection nextConn = randomConnection(connection.server);
                RandomWalkMessage rwm = (RandomWalkMessage) received;
                long prevT = rwm.getT();
                long nextT = System.nanoTime();
                rwm.setT(nextT);


                int hops = rwm.hops;

                if (hops < 25000) {
                    hist.recordValue((nextT - prevT) / 1000);
                }

                if (hops > 0) {
                    rwm.hops = hops - 1;
                    nextConn.send(received);
                } else {
                    System.out.println("Zero hops " + cnt.incrementAndGet());
                    if (cnt.get() == 45) {
                        for (int i = 75; i <= 100; i++) {
                            System.out.println(i + "% " + hist.getValueAtPercentile(i));
                        }
                    }
                }
            }
        }
    }

    private Connection randomConnection(Server server) {
        int next = rnd.nextInt(server.in.size() + server.out.size());
        Connection nextConn;
        if (next < server.in.size()) {
            nextConn = server.in.get(next);
        } else {
            nextConn = server.out.get(next - server.in.size());
        }
        return nextConn;
    }

    private void handleWrite(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();

        connection.writeBuf();
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (!channel.finishConnect()) {
            cancelAndClose(key);
            return;
        }

        Connection connection = (Connection) key.attachment();
        key.interestOps(OP_READ);

        PingMessage msg = new PingMessage();
        msg.setPort(connection.portFrom);
        connection.send(msg);

        connection.server.out.add(connection);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel servChannel = (ServerSocketChannel) key.channel();
        SocketChannel inChannel = servChannel.accept();
        Server server = (Server) key.attachment();
        inChannel.configureBlocking(false);

        SelectionKey inKey = inChannel.register(selector, OP_READ);
        Connection conn = new Connection(server, inChannel, 0, server.port, inKey);
        inKey.attach(conn);

        server.in.add(conn);
    }

    private void cancelAndClose(SelectionKey key) {
        SelectableChannel channel = key.channel();
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            // skip
        }
    }

    private void connect(Server server, Selector sel, int portFrom, int portTo) throws IOException {
        SocketChannel socketChannel = sel.provider().openSocketChannel();
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(sel, OP_CONNECT);
        Connection connection = new Connection(server, socketChannel, portFrom, portTo, key);
        key.attach(connection);
        socketChannel.connect(new InetSocketAddress(portTo));
    }

    private Server createServer(Selector sel, int port) throws IOException {
        ServerSocketChannel serverChannel = sel.provider().openServerSocketChannel();
        serverChannel.configureBlocking(false);
        Server server = new Server(port, serverChannel);
        serverChannel.register(sel, OP_ACCEPT, server);
        serverChannel.bind(new InetSocketAddress(server.port));
        return server;
    }

    private class Server {
        final int port;
        final ServerSocketChannel channel;
        List<Connection> out;
        List<Connection> in;

        public Server(int port, ServerSocketChannel serverChannel) {
            this.port = port;
            channel = serverChannel;
            out = new ArrayList<>();
            in = new ArrayList<>();
        }
    }

    @ToString(of = {"portFrom", "portTo"})
    public class Connection {
        private Server server;
        private SocketChannel channel;
        private int portFrom;
        private final int portTo;
        private final SelectionKey key;

        private ByteBuffer inBuffer;
        private ByteBuffer deserializeBuf;
        private In in;

        private ByteBuffer outBuffer;
        private CountOut countOut;
        private Out out;

        public Connection(Server server, SocketChannel channel, int portFrom, int portTo, SelectionKey key) {
            this.server = server;
            this.channel = channel;
            this.portFrom = portFrom;
            this.portTo = portTo;
            this.key = key;

            inBuffer = ByteBuffer.allocate(4096);
            deserializeBuf = inBuffer.duplicate();
            in = new BufIn(deserializeBuf);

            outBuffer = ByteBuffer.allocate(4096);
            countOut = new CountOut();
            out = new BufOut(outBuffer);
        }

        public Serializable receive() throws IOException {
            channel.read(inBuffer);

            int frameStart = 4;
            int bufSize = inBuffer.position();
            if (bufSize < frameStart) {
                return null;
            }

            int frameEnd = inBuffer.getInt(0) + frameStart;

            // TODO reallocate buf if needed
            // TODO SKIP DATA if frame too big
            if (bufSize < frameEnd) {
                return null;
            }

            deserializeBuf.position(frameStart).limit(frameEnd);
            inBuffer.position(frameEnd).limit(bufSize);

            int tag = deserializeBuf.getInt();
            int idx = typeMapping.idxByTag(tag);

            Supplier<Serializable> supplier = typeMapping.getConstructor(idx);

            Serializable result = supplier.get();

            result.deserialize(in);

            inBuffer.compact();

            return result;
        }


        public void send(Serializable msg) {
            countOut.reset();
            msg.serialize(countOut);
            int requiredSpace = countOut.getSize() + Integer.BYTES * 2;
            reallocateOut(requiredSpace);

            int sizePos = outBuffer.position();
            outBuffer.putInt(0);
            int frameStart = outBuffer.position();
            outBuffer.putInt(typeMapping.tagByChoiceType(msg.getType()));
            msg.serialize(out);
            int frameEnd = outBuffer.position();

            outBuffer.putInt(sizePos, frameEnd - frameStart);

            key.interestOps(key.interestOps() | OP_WRITE);
        }

        public void writeBuf() throws IOException {
            outBuffer.flip();
            channel.write(outBuffer);
            if (outBuffer.remaining() == 0) {
                key.interestOps(key.interestOps() & ~OP_WRITE);
            }
            outBuffer.compact();
        }

        private void reallocateOut(int requiredSpace) {
            if (outBuffer.remaining() >= requiredSpace) {
                return;
            }
            ByteBuffer prevBuf = outBuffer;
            int newSize = outBuffer.capacity() * 2;
            while (newSize - outBuffer.position() < requiredSpace) {
                newSize *= 2;
            }
            outBuffer = ByteBuffer.allocate(newSize);
            out = new BufOut(outBuffer);
            prevBuf.flip();
            outBuffer.put(prevBuf);
        }
    }
}
