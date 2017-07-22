package node.datagram.handlers;

import lombok.extern.slf4j.Slf4j;
import node.Address;
import node.Message;
import node.Party;
import node.datagram.ServerSocketParty;
import node.datagram.SocketParty;
import node.datagram.event.ReadEvent;
import node.datagram.SocketGossipNodeShared;
import node.datagram.event.Event;
import node.pong.PingMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static node.datagram.event.ReadEvent.READ_EVENT;
import static node.datagram.event.RegisterPartyEvent.REGISTER_PARTY_EVENT;

@Slf4j
public class SelectorTask implements Runnable {
    private final Dispatcher<Event> dispatcher;
    private final Selector selector;
    private final SocketGossipNodeShared shared;
    private final ConcurrentLinkedQueue<Party> toRegister;
    private Publisher publisher;
    private int readBufSize;

    public SelectorTask(SocketGossipNodeShared shared, ConcurrentLinkedQueue<Party> toRegister, int readBufSize) {
        selector = shared.getSelector();
        dispatcher = shared.getReadProcessDispatcher();
        this.shared = shared;
        this.toRegister = toRegister;
        this.readBufSize = readBufSize;
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

        ByteBuffer writeBuf = ByteBuffer.allocateDirect(4096);

        AtomicInteger cnt = new AtomicInteger();
        AtomicInteger cnt2 = new AtomicInteger();
        AtomicInteger cnt3 = new AtomicInteger();
        Set<Integer> set = new TreeSet<>();

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

            if (key.isAcceptable()) {
                ServerSocketParty party = (ServerSocketParty) key.attachment();

                event.setSelf(party);
                event.setShared(shared);

                try {
                    SocketChannel channel = party.getChannel().accept();
                    channel.configureBlocking(false);

                    Address address = new Address();
                    address.copyFromObj(channel.getRemoteAddress());

                    SocketParty socketParty = new SocketParty(address,
                            party.getGossipNode(),
                            channel,
                            party.getGossipNode().getFactory().createWriteQueue());

                    System.out.println("accept " + cnt2.incrementAndGet());
                    event.activateSubEvent(REGISTER_PARTY_EVENT)
                            .setParty(socketParty);

                } catch (IOException ex) {
                    closeAndCancel(key, ex);
                }

            } else if (key.isReadable()) {
                try {
                    SocketParty party = (SocketParty) key.attachment();

                    event.setSelf(party);
                    event.setShared(shared);

                    ByteBuffer buffer = party.getReadBuffer();

                    party.getChannel().read(buffer);
                    int frameStart = 4;
                    int readBytes = buffer.position();
                    if (readBytes < frameStart) {
                        System.out.println("ret " + readBytes + "!");
                        return;
                    }
                    int frameEnd = buffer.getInt(0) + frameStart;
                    if (readBytes < frameEnd) {
                        System.out.println("ret " + readBytes);
                        return;
                    }
                    buffer.position(frameStart).limit(frameEnd);

                    ReadEvent readEvent = event.getSubEvent().activate(READ_EVENT);
                    Message message = readEvent.getMessage();
                    message.deserialize(buffer);

                    buffer.position(frameEnd).limit(readBytes);
                    buffer.compact();

                    System.out.println("read " + cnt.incrementAndGet() + " " + buffer.remaining());

                    if (message.instanceOf(PingMessage.TYPE)) {
                        int val = message.castTo(PingMessage.TYPE).getValue();
                        set.add(val);
                        System.out.println(set);
                    }


                    log.trace("Received {} bytes. {} deserialized. A {} from {}", readBytes, frameEnd, message, party);

                    //                SocketAddress receiveAddress =
                    //                message.getSender().copyFromObj(receiveAddress);

                } catch (IOException ex) {
                    closeAndCancel(key, ex);
                }
            } else if (key.isWritable()) {
                SocketParty party = (SocketParty) key.attachment();

                try {
                    Message writeMsg = party.getWriteMsg();
                    writeMsg.clear();

                    party.getWriteQ().deQ(true, writeMsg::copyFrom);

                    if (writeMsg.getSubType().isActive()) {
                        writeBuf.clear();
                        writeBuf.putInt(0);
                        int frameStart = writeBuf.position();
                        writeMsg.serialize(writeBuf);
                        int frameEnd = writeBuf.position();
                        writeBuf.putInt(0, frameEnd - frameStart);
                        writeBuf.flip();
                        int n = party.getChannel().write(writeBuf);
                        writeBuf.clear();
                        log.trace("Sent {} bytes. A {} from {}", n, writeMsg, party);
                        System.out.println("write " + cnt3.incrementAndGet() + " " + n);
                    }
                    if (party.getWriteQ().isEmpty()) {
                        party.notInterestedInWrite();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // skip
                } catch (IOException ex) {
                    closeAndCancel(key, ex);
                }
            }
        }

        private void closeAndCancel(SelectionKey key, IOException ex) {
            log.trace("Channel closed", ex);
            try {
                key.channel().close();
            } catch (IOException e) {
                log.warn("Error closing channel", e);
            }
            key.cancel();
        }
    }

    private void registerFromQueue() throws ClosedChannelException {
        Party party;
        while ((party = toRegister.poll()) != null) {
            if (party instanceof SocketParty) {
                log.trace("Registering {} for selection for reading", party);

                SocketParty socketParty = (SocketParty) party;

                socketParty.allocateReadBuffer(readBufSize);

                SelectionKey key = socketParty.getChannel()
                        .register(selector,
                                SelectionKey.OP_READ,
                                party);

                socketParty.setSelectionKey(key);


//                socketParty.getWriteQ()
//                        .onEmpty(() -> {
//                            int ops = key.interestOps();
//                            key.interestOps(~SelectionKey.OP_WRITE & ops);
//                            System.out.println("off ops " + ((ops & SelectionKey.OP_WRITE) > 0));
//                        });

            } else if (party instanceof ServerSocketParty) {
                log.trace("Registering {} for selection for accepting", party);

                ServerSocketParty serverSocketParty = (ServerSocketParty) party;

                serverSocketParty.getChannel()
                        .register(selector, SelectionKey.OP_ACCEPT, party);

            }
        }
    }
}
