package gossip;

import lombok.Getter;
import gossip.message.Message;
import gossip.message.MessageType;
import gossip.registry.Registry;
import gossip.registry.RegistryMapping;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.*;

public class SocketShared {
    @Getter
    private final Selector selector;
    @Getter
    private final RegistryMapping<MessageType<Message>, Message> messageTypes;
    private final int maxInMsgSize;

    boolean done = false;

    public SocketShared(Registry<?> registry,
                        int maxInMsgSize) throws IOException {
        this.selector = Selector.open();
        this.messageTypes = registry.mapping(MessageType.class);
        this.maxInMsgSize = maxInMsgSize;
    }


    public void loopSelector() throws IOException {
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
