package node2;

import lombok.Getter;
import node2.message.Message;
import node2.message.MessageType;
import node2.registry.RegistryMapping;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;

public class SocketGossip {
    @Getter
    private final int port;
    private final ServerSocketChannel serverChannel;
    private final List<SocketParty> in;
    private final List<SocketParty> out;
    @Getter
    private final LedgerImpl<Message> ledger;
    @Getter
    private final SocketShared shared;
    private final Supplier<Integer> portGen;
    private final RegistryMapping<MessageType<Message>, Message> typeMapping;
    private final BiConsumer<SocketParty, Message> handler;

    public SocketGossip(SocketShared shared, Supplier<Integer> portGen, BiConsumer<SocketParty, Message> handler) throws IOException {
        this.shared = shared;
        this.portGen = portGen;
        this.handler = handler;
        typeMapping = getShared().getMessageTypes();
        Selector selector = shared.getSelector();
        serverChannel = selector.provider().openServerSocketChannel();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, OP_ACCEPT, this);
        int bindPort = -1;
        for (int i = 0; i < 65536; i++) {
            try {
                bindPort = portGen.get() & 0xFFFF;
                serverChannel.bind(new InetSocketAddress(bindPort));
                break;
            } catch (AlreadyBoundException ex) {
                bindPort = -1;
            }
        }
        if (bindPort == -1) {
            throw new AlreadyBoundException();
        }

        this.port = bindPort;
        out = new ArrayList<>();
        in = new ArrayList<>();

        ledger = new LedgerImpl<>(
                1024,
                TimeUnit.SECONDS.toMillis(60),
                shared.getMessageTypes()::reuse);
    }

    void connect(InetSocketAddress address) throws IOException {
        Selector selector = shared.getSelector();
        SocketChannel socketChannel = selector.provider().openSocketChannel();
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(selector, OP_CONNECT);

        SocketParty connection = new SocketParty(
                this, socketChannel, key, 64);

        key.attach(connection);
        socketChannel.connect(address);
    }

    public static int RNG = 0;
    public SocketParty randomConnection() {
        int next = RNG++ % (in.size() + out.size());
        SocketParty nextConn;
        if (next < in.size()) {
            nextConn = in.get(next);
        } else {
            nextConn = out.get(next - in.size());
        }
        return nextConn;
    }

    public void addOut(SocketParty party) {
        out.add(party);
    }

    public void addIn(SocketParty party) {
        in.add(party);
    }

    public void processMessage(Message msg, SocketParty party) {
        handler.accept(party, msg);
//        getLedger().add(msg.getId(), System.currentTimeMillis(), msg)

    }
}
