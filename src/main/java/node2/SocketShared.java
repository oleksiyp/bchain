package node2;

import lombok.Getter;
import node2.in_out.*;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.*;
import static node2.in_out.Registry.emptyRegistry;

public class SocketShared {
    public static final int PORT_START = 2000;
    @Getter
    private final Selector selector;
    @Getter
    private final RegistryMapping<MessageType<Message>, Message> typeMapping;
    private final int maxInMsgSize;

    boolean done = false;

    public SocketShared(RegistryMapping<MessageType<Message>, Message> typeMapping,
                        int maxInMsgSize) throws IOException {
        this.selector = Selector.open();
        this.typeMapping = typeMapping;
        this.maxInMsgSize = maxInMsgSize;
    }

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();

        SocketShared shared = new SocketShared(emptyRegistry()
                .register(0x0, PingMessage.TYPE, PingMessage::new)
                .register(0x1, PongMessage.TYPE, PongMessage::new)
                .register(0x2, RandomWalkMessage.TYPE, RandomWalkMessage::new)
                .mapping(MessageType.class), 64);

        List<SocketGossip> servers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int iCopy = i;
            Supplier<Integer> portGen = () -> iCopy + PORT_START;
            servers.add(new SocketGossip(shared, portGen));
        }

        for (int i = 0; i < servers.size(); i++) {
            for (int j = i + 1; j < servers.size(); j++) {
                SocketGossip from = servers.get(i);
                SocketGossip to = servers.get(j);
                from.connect(new InetSocketAddress(to.getPort()));
            }
        }

        shared.run();


        long end = System.nanoTime();
        System.out.printf("%.3f seconds%n", (end - start) / 1e9);
    }

    private void run() throws IOException {
        while (!done) {
            if (selector.selectNow() == 0) continue;

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
        SocketParty party = (SocketParty) key.attachment();
        SocketGossip gossip = party.getGossip();

        Message received;

        while ((received = party.receive()) != null) {
            gossip.processMessage(received, party);
        }
    }


    private void handleWrite(SelectionKey key) throws IOException {
        SocketParty party = (SocketParty) key.attachment();
        party.writeBuf();
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (!channel.finishConnect()) {
            cancelAndClose(key);
            return;
        }

        SocketParty party = (SocketParty) key.attachment();
        party.interested(OP_READ);

        PingMessage msg = new PingMessage();
        msg.setPort(party.getGossip().getPort());
        party.send(msg);

        party.getGossip().addOut(party);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel servChannel = (ServerSocketChannel) key.channel();
        SocketChannel inChannel = servChannel.accept();
        SocketGossip gossip = (SocketGossip) key.attachment();
        inChannel.configureBlocking(false);

        SelectionKey inKey = inChannel.register(selector, OP_READ);
        SocketParty party = new SocketParty(gossip, inChannel, inKey, maxInMsgSize);
        inKey.attach(party);

        gossip.addIn(party);
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

}
