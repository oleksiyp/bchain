package node;

import node.counter.CountNodesTypes;
import node.factory.GossipFactoryImpl;
import node.counter.AckCountNodesMessage;
import node.counter.CountNodesMessage;
import node.datagram.DatagramGossipNode;
import node.datagram.DatagramGossipNodeShared;
import node.ledger.UberActor;
import node.ledger.ActorContext;
import node.ledger.Ledger;
import node.pong.PongActor;
import node.pong.PongTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.MappedQueue;
import util.mutable.Mutable;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static node.factory.GossipTypes.*;

public class CountNodesActorTest {

    private Gossip node1;
    private Gossip node2;
    private Gossip node3;
    private Gossip node4;

    private Supplier<Long> idGenerator;
    private Supplier<Integer> portGenerator;
    private DatagramGossipNodeShared shared;
    private GossipFactoryImpl factory;
    private List<Gossip> gossips;

    @Before
    public void setUp() throws Exception {
        factory = new GossipFactoryImpl(
                eventRegistry(),
                messageRegistry()
                        .merge(0x100, CountNodesTypes.messageRegistry())
                        .merge(0x200, PongTypes.messageRegistry()),
                headersRegistry(),
                actorRegistry()
                        .merge(0x100, CountNodesTypes.actorRegistry())
                        .merge(0x200, PongTypes.actorRegistry()));

        shared = new DatagramGossipNodeShared(
                16 * 1024,
                1024,
                factory);

        Random rnd = new Random();

        AtomicInteger next = new AtomicInteger(2000);
        idGenerator = rnd::nextLong;
        portGenerator = () -> next.getAndIncrement() % 65536;

        node1 = createNode(shared);
        node2 = createNode(shared);
        node3 = createNode(shared);
        node4 = createNode(shared);

        gossips = new ArrayList<>();
        gossips.add(node1);
        gossips.add(node2);
        gossips.add(node3);
        gossips.add(node4);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(PongActor.join(node1, node2.address()));
        futures.add(PongActor.join(node3, node2.address()));
        futures.add(PongActor.join(node3, node1.address()));
        futures.add(PongActor.join(node4, node2.address()));

        allOf(futures.toArray(new CompletableFuture[0]))
                .get();
    }

    @Test
    public void countNodes() throws Exception {

        AtomicLong actual = new AtomicLong();
        CountDownLatch latch = new CountDownLatch(1);

        node3.listen(AckCountNodesMessage.ACK_COUNT_NODES_MESSAGE, (msg, ackCount) -> {
            actual.set(ackCount.getCount());
            latch.countDown();
        });

        node3.send(new Message(factory,
                CountNodesMessage.COUNT_NODES_MESSAGE_MESSAGE));

        latch.await();
        Assert.assertEquals(4, actual.get());

    }

    private DatagramGossipNode createNode(DatagramGossipNodeShared shared) throws IOException {
        MappedQueue<UberActor> mappedQueue = new MappedQueue<>(
                1024,
                SECONDS.toMillis(60),
                shared.getFactory()::createUberActor,
                Mutable::clear);

        ActorContext context = new ActorContext();
        DatagramGossipNode node = new DatagramGossipNode(
                shared,
                Inet4Address.getByName("localhost"),
                Inet4Address.getByName("localhost"),
                portGenerator,
                idGenerator,
                new Ledger(mappedQueue, context));

        return node;
    }
}