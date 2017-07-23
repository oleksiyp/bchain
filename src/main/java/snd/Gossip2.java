package snd;

import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import static java.nio.channels.SelectionKey.*;

public class Gossip2 {
    public static final int PORT_START = 2000;
    private List<Server> servers;
    private Selector selector;

    public static void main(String[] args) throws IOException {
        new Gossip2().run();
    }

    private void run() throws IOException {
        selector = Selector.open();

        servers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            servers.add(createServer(selector, i + PORT_START));
        }

        for (int i = 0; i < servers.size(); i++) {
            for (int j = i + 1; j < servers.size(); j++) {
                Connection connection = connect(selector, servers.get(i).port, servers.get(j).port);
                servers.get(i).out.add(connection);
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

    private void handleRead(SelectionKey key) throws IOException {
        // TODO framing
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(4);
        int n = channel.read(buf);
        if (n == 4) {
            Connection conn = (Connection) key.attachment();
            conn.portFrom = buf.getInt(0);
            System.out.println("Fully accepted " + conn);
        }
    }

    private void handleWrite(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        Sender sendReq = connection.nextWriteJob();
        if (sendReq != null) {
            System.out.println("Writing " + connection);
            sendReq.send((SocketChannel) key.channel());
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (!channel.finishConnect()) {
            cancelAndClose(key);
            return;
        }
        Connection connection = (Connection) key.attachment();
        System.out.println("Connected " + connection);

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(0, connection.portFrom);
        key.interestOps(OP_READ);
        connection.send((ch) -> {
            try {
                ch.write(buf);
            } catch (IOException e) {
                System.out.println("Error");
                // skip
            }
        });
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel servChannel = (ServerSocketChannel) key.channel();
        SocketChannel inChannel = servChannel.accept();
        Server server = (Server) key.attachment();
        inChannel.configureBlocking(false);
        SelectionKey inKey = inChannel.register(selector, OP_READ);
        Connection conn = new Connection(inChannel, 0, server.port, inKey);
        inKey.attach(conn);
        System.out.println("Accepted " + conn);
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

    private Connection connect(Selector sel, int portFrom, int portTo) throws IOException {
        SocketChannel socketChannel = sel.provider().openSocketChannel();
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(sel, OP_CONNECT);
        Connection connection = new Connection(socketChannel, portFrom, portTo, key);
        key.attach(connection);
        socketChannel.connect(new InetSocketAddress(portTo));
        return connection;
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
    private class Connection {
        private SocketChannel channel;
        private int portFrom;
        private final int portTo;
        private final SelectionKey key;
        public Queue<Sender> outQ;

        public Connection(SocketChannel channel, int portFrom, int portTo, SelectionKey key) {
            this.channel = channel;
            this.portFrom = portFrom;
            this.portTo = portTo;
            this.key = key;
            outQ = new ArrayDeque<>();
        }

        public void send(Sender consumer) {
            outQ.add(consumer);
            key.interestOps(key.interestOps() | OP_WRITE);
        }

        public Sender nextWriteJob() {
            Sender consumer = outQ.poll();
            if (consumer == null) {
                key.interestOps(key.interestOps() & ~OP_WRITE);
            }
            return consumer;
        }
    }

    interface Sender {
        void send(SocketChannel channel);
    }
}
