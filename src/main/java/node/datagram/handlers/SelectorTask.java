package node.datagram.handlers;

import lombok.extern.slf4j.Slf4j;
import node.datagram.Message;
import node.datagram.Party;
import node.datagram.event.ReadEvent;
import node.datagram.shared.Dispatcher;
import node.datagram.shared.GossipNodeShared;
import node.datagram.event.Event;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

import static node.datagram.event.EventType.READ_EVENT;

@Slf4j
public class SelectorTask implements Runnable {
    private final Dispatcher<Event> dispatcher;
    private final Selector selector;
    private final GossipNodeShared shared;
    private final ConcurrentLinkedQueue<Party> toRegister;
    private final ByteBuffer buffer;
    private Publisher publisher;

    public SelectorTask(GossipNodeShared shared, ConcurrentLinkedQueue<Party> toRegister, int bufSize) {
        selector = shared.getSelector();
        dispatcher = shared.getReadProcessDispatcher();
        this.shared = shared;
        this.toRegister = toRegister;
        buffer = ByteBuffer.allocateDirect(bufSize);
        publisher = new Publisher();
    }

    @Override
    public void run() {
        try {
            while (true) {
                int nn = selector.select();

                registerFromQueue();
                selector.selectNow();

                if (nn == 0) {
                    continue;
                }

                Set<SelectionKey> keys = selector.selectedKeys();

                if (log.isDebugEnabled()) {
                    log.trace("Selected {} keys (n = {})", keys.size(), nn);
                }

                publisher.init(keys.iterator());
                dispatcher.dispatch(keys.size(), publisher);
                publisher.init(null);
            }
        } catch (IOException e) {
            // skip
        }
    }

    private class Publisher implements BiConsumer<Integer, Event> {
        private Iterator<SelectionKey> keysIterator;

        public void init(Iterator<SelectionKey> keysIterator) {
            this.keysIterator = keysIterator;
        }

        @Override
        public void accept(Integer i, Event event) {
            if (!keysIterator.hasNext()) {
                return;
            }
            SelectionKey key = keysIterator.next();
            keysIterator.remove();

            if (!key.isValid()) {
                return;
            }

            if (!key.isReadable()) {
                return;
            }

            try {
                Party party = (Party) key.attachment();

                event.setSelf(party);
                event.setShared(shared);

                buffer.clear();
                party.getChannel().receive(buffer);
                buffer.flip();
                if (buffer.remaining() == 0) {
                    return;
                }

                ReadEvent readEvent = event.getSubEvent().activate(READ_EVENT);
                Message message = readEvent.getMessage();
                message.deserialize(buffer);
                buffer.flip();


                log.trace("Received {} bytes. A {} from {}", buffer.remaining(), message, party);

//                SocketAddress receiveAddress =
//                message.getSender().copyFromObj(receiveAddress);

            } catch (IOException ex) {
                log.trace("Channel closed", ex);
                try {
                    key.channel().close();
                } catch (IOException e) {
                    log.warn("Error closing channel", e);
                }
                key.cancel();
            }
        }
    }

    private void registerFromQueue() throws ClosedChannelException {
        Party party;
        while ((party = toRegister.poll()) != null) {
            log.trace("Registering {} for selection for reading", party);
            party.getChannel().register(selector,
                    SelectionKey.OP_READ,
                    party);
        }
    }
}
