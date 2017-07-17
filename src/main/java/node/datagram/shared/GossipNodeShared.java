package node.datagram.shared;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import node.datagram.GossipFactory;
import node.datagram.Party;
import node.datagram.event.Event;
import node.datagram.handlers.*;
import util.Cancelable;
import util.DisruptorUtil;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class GossipNodeShared implements Cancelable {
    private final Selector selector;
    private final Disruptor<Event> readProcessDisruptor;
    private final Disruptor<Event> writeDisruptor;

    private final Dispatcher<Event> readProcessDispatcher;
    private final Dispatcher<Event> writeDispatcher;
    private final ConcurrentLinkedQueue<Party> toRegister;
    private final GossipFactory factory;
    private final Thread selectorThread;

    public GossipNodeShared(int ringBufferSize,
                            int writeBufSize,
                            GossipFactory factory) throws IOException {
        this.factory = factory;
        selector = Selector.open();

        readProcessDisruptor = new Disruptor<>(
                () -> new Event(factory),
                ringBufferSize,
                new DefaultThreadFactory("gossip-read"),
                ProducerType.MULTI,
                new BlockingWaitStrategy());

        readProcessDisruptor
                .handleEventsWith(DisruptorUtil.seq(
                        new ReadIsSendHandler(factory),
                        new LedgerHandler(),
                        new MessageHandler(),
                        new BroadcastHandler(),
                        new ClearHandler()));

        readProcessDisruptor.start();

        readProcessDispatcher = new DisruptorDispatcher<>(readProcessDisruptor);

        writeDisruptor = new Disruptor<>(
                () -> new Event(factory),
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

        SelectorTask selectorTask = new SelectorTask(this, toRegister, 4 * 1024);
        selectorThread = new Thread(selectorTask, "selector");
        selectorThread.start();
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
        selectorThread.interrupt();
        selector.wakeup();
        try {
            selector.close();
        } catch (IOException e) {
            // skip
        }
        readProcessDisruptor.shutdown();
        writeDisruptor.shutdown();
    }

    public GossipFactory getFactory() {
        return factory;
    }
}
