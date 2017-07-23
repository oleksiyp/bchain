package node2;

import lombok.ToString;
import node2.in_out.*;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.*;
import static node2.in_out.Registry.emptyRegistry;

public class Gossip2 {
    public static final int PORT_START = 2000;
    private List<Server> servers;
    private Selector selector;
    private RegistryMapping<MessageType<Serializable>, Serializable> typeMapping;

    boolean done = false;

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        new Gossip2().run();
        long end = System.nanoTime();
        System.out.printf("%.3f seconds%n", (end - start) / 1e9);
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

        while (!done) {
            if (selector.select() == 0) continue;

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
    int cnt = 0;

    Histogram hist = new Histogram(3);

    private void handleRead(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();

        Serializable received;

        while ((received = connection.receive()) != null) {
            processMessage(connection, received);
        }
    }

    int nMsgs = 0;
    long nSends = 0;
    int n = 0;
    private void processMessage(Connection connection, Serializable received) {
        if (received instanceof PingMessage) {
            PingMessage ping = (PingMessage) received;
            connection.portFrom = ping.getPort();

            PongMessage pong = new PongMessage();
            pong.port = ping.port;
            connection.send(pong);
        } else if (received instanceof PongMessage) {
            RandomWalkMessage rwm = typeMapping.create(RandomWalkMessage.TYPE);
            rwm.setHops(10000);
            rwm.setT(System.nanoTime());
            for (int i = 0; i < 1000; i++) {
                connection.send(rwm);
                nMsgs++;
            }

            typeMapping.reuse(RandomWalkMessage.TYPE, rwm);
        } else if (received instanceof RandomWalkMessage) {
            nSends++;
            Connection nextConn = randomConnection(connection.server);
            RandomWalkMessage rwm = (RandomWalkMessage) received;
            long prevT = rwm.getT();
            long nextT = System.nanoTime();
            rwm.setT(nextT);
            
            int hops = rwm.hops;

            if (hops < 3000) {
                long mks = (nextT - prevT) / 1000;
                hist.recordValue(mks);
            }

            if (hops > 0) {
                rwm.hops = hops - 1;
                nextConn.send(received);
            } else {
//                System.out.println("Zero hops " + ++cnt);
                cnt++;
                if (cnt == nMsgs) {
                    for (double i = 90; i <= 100; i += 0.5) {
                        System.out.println(i + "% " + hist.getValueAtPercentile(i));
                    }
                    done = true;
                    System.out.println(nSends + " messages");
                }
            }

            typeMapping.poolByChoice(RandomWalkMessage.TYPE).accept(rwm);
        }
    }

    int rng = 0;
    private Connection randomConnection(Server server) {
        int next = rng++ % (server.in.size() + server.out.size());
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
        connection.interested(OP_READ);

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
        Connection conn = new Connection(server, inChannel, 0, server.port, inKey, 64);
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
        Connection connection = new Connection(server, socketChannel, portFrom, portTo, key, 64);
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
        private int maxInMsgSize;
        private int skipNextNBytes;
        private int ops;

        public Connection(Server server, SocketChannel channel, int portFrom, int portTo, SelectionKey key, int maxInMsgSize) {
            this.server = server;
            this.channel = channel;
            this.portFrom = portFrom;
            this.portTo = portTo;
            this.key = key;
            this.maxInMsgSize = maxInMsgSize;

            inBuffer = ByteBuffer.allocate(64 * 1024);
            deserializeBuf = inBuffer.duplicate();
            in = new BufIn(deserializeBuf);

            outBuffer = ByteBuffer.allocate(512);
            countOut = new CountOut();
            out = new BufOut(outBuffer);
            ops = key.interestOps();
        }

        public Serializable receive() throws IOException {
            while (true) {
                int bufSize = inBuffer.position();

                int n = Math.min(bufSize, skipNextNBytes);
                if (n > 0) {
                    inBuffer.position(n).limit(bufSize);
                    inBuffer.compact();
                    skipNextNBytes -= n;
                    continue;
                }

                int frameStart = 4;

                if (frameStart > bufSize) {
                    channel.read(inBuffer);
                    bufSize = inBuffer.position();
                    if (frameStart > bufSize) {
                        return null;
                    }
                }

                int frameEnd = inBuffer.getInt(0) + frameStart;

                if (frameEnd >= maxInMsgSize) {
                    skipNextNBytes = frameEnd;
                    continue;
                } else if (frameEnd > inBuffer.capacity()) {
                    reallocateIn(frameEnd);
                    continue;
                } else if (frameEnd > bufSize) {
                    channel.read(inBuffer);
                    bufSize = inBuffer.position();
                    if (frameEnd > bufSize) {
                        return null;
                    }
                }

                deserializeBuf.position(frameStart).limit(frameEnd);
                inBuffer.position(frameEnd).limit(bufSize);

                try {
                    int tag = deserializeBuf.getInt();
                    int idx = typeMapping.idxByTag(tag);

                    Supplier<Serializable> supplier = typeMapping.constructorByIdx(idx);

                    Serializable result = supplier.get();

                    result.deserialize(in);

                    return result;
                } finally {
                    inBuffer.compact();
                }
            }
        }

        private void reallocateIn(int bufSz) {
            ByteBuffer prevBuf = inBuffer;
            int newSize = inBuffer.capacity() * 2;
            while (newSize < bufSz) {
                newSize *= 2;
            }
            inBuffer = ByteBuffer.allocate(newSize);
            deserializeBuf = inBuffer.duplicate();
            in = new BufIn(deserializeBuf);
            prevBuf.flip();
            inBuffer.put(prevBuf);
        }

        public void writeBuf() throws IOException {
            outBuffer.flip();
            channel.write(outBuffer);
            if (outBuffer.remaining() == 0) {
                interested(ops & ~OP_WRITE);
            }
            outBuffer.compact();
        }

        public void interested(int newOps) {
            if (ops != newOps) {
                ops = newOps;
                key.interestOps(ops);
            }
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

            interested(ops | OP_WRITE);
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
