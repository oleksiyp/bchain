package node.datagram.shared;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import node.datagram.Message;
import node.datagram.Party;
import node.datagram.event.Event;
import node.datagram.handlers.*;
import util.Cancelable;
import util.DisruptorUtil;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GossipNodeShared implements Cancelable {
    private final Selector selector;
    private final Disruptor<Event> readProcessDisruptor;
    private final Disruptor<Event> writeDisruptor;

    private final Dispatcher<Event> readProcessDispatcher;
    private final Dispatcher<Event> writeDispatcher;
    private final ConcurrentLinkedQueue<Party> toRegister
            ;

    public GossipNodeShared(int ringBufferSize, int readBufSize, int writeBufSize, Supplier<Message> messageFactory) throws IOException {
        selector = Selector.open();

        readProcessDisruptor = new Disruptor<>(
                () -> new Event(messageFactory),
                ringBufferSize,
                new DefaultThreadFactory("gossip-read"),
                ProducerType.MULTI,
                new BlockingWaitStrategy());

        readProcessDisruptor
                .handleEventsWith(DisruptorUtil.seq(
                        new LedgerHandler(),
                        new MessageHandler(),
                        new BroadcastHandler(),
                        new ClearHandler()));

        readProcessDisruptor.start();

        readProcessDispatcher = new DisruptorDispatcher<>(readProcessDisruptor);

        writeDisruptor = new Disruptor<>(
                () -> new Event(messageFactory),
                ringBufferSize,
                new DefaultThreadFactory("gossip-write"),
                ProducerType.SINGLE,
                new BlockingWaitStrategy());

        writeDisruptor
                .handleEventsWith(
                        DisruptorUtil.seq(
                                new WriteHandler(writeBufSize),
                                new ClearHandler()));

        writeDisruptor.start();

        writeDispatcher = new DisruptorDispatcher<>(writeDisruptor);

        toRegister = new ConcurrentLinkedQueue<>();
        new Thread(new SelectorTask(this, toRegister, 4 * 1024),"selector").start();
    }

    public Selector getSelector() {
        return selector;
    }

    public Consumer<Party> getPartyRegistrar() {
        return (party) -> {
            toRegister.add(party);
            selector.wakeup();
        };
    }

    public Dispatcher<Event> getReadProcessDispatcher() {
        return readProcessDispatcher;
    }

    public Dispatcher<Event> getWriteDispatcher() {
        return writeDispatcher;
    }

    @Override
    public void cancel() {
        try {
            selector.close();
        } catch (IOException e) {
            // skip
        }
        readProcessDisruptor.shutdown();
        writeDisruptor.shutdown();
    }
}
