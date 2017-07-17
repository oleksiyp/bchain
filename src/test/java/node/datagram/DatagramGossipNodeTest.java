package node.datagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.counter.AckCountNodesMessage;
import node.datagram.counter.CountNodesMessage;
import node.datagram.counter.CountNodesActor;
import node.datagram.ledger.UberActor;
import node.datagram.ledger.ActorContext;
import node.datagram.ledger.Ledger;
import node.datagram.shared.GossipNodeShared;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.MappedQueue;
import util.Serializable;
import util.mutable.Mutable;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DatagramGossipNodeTest {

    private Gossip node1;
    private Gossip node2;
    private Gossip node3;
    private Gossip node4;

    public static final MessageType<TestMessage> TEST_MESSAGE = new MessageType<>(
            5,
            "TEST",
            TestMessage.class,
            TestMessage::new);

    private Supplier<Long> idGenerator;
    private Supplier<Integer> portGenerator;
    private GossipNodeShared shared;
    private GossipFactoryImpl factory;
    private List<Gossip> gossips;

    @Before
    public void setUp() throws Exception {
        factory = new GossipFactoryImpl();

        factory.register(TEST_MESSAGE);
        factory.register(CountNodesActor.TYPE);
        factory.register(CountNodesMessage.TYPE);
        factory.register(AckCountNodesMessage.TYPE);

        shared = new GossipNodeShared(
                16 * 1024,
                1024,
                factory);

        Random rnd = new Random();

        idGenerator = rnd::nextLong;
        portGenerator = () -> rnd.nextInt(65536);

        node1 = createNode(shared);
        node2 = createNode(shared);
        node3 = createNode(shared);
        node4 = createNode(shared);

        gossips = new ArrayList<>();
        gossips.add(node1);
        gossips.add(node2);
        gossips.add(node3);
        gossips.add(node4);

        node1.join(node2.address());
        node3.join(node2.address());
        node3.join(node1.address());
//        node4.join(node2.address());

        SECONDS.sleep(2);
    }

    @Test
    public void send1Mesage() throws Exception {

        AtomicLong actual = new AtomicLong();
        CountDownLatch latch = new CountDownLatch(1);

        node3.listen(AckCountNodesMessage.TYPE, (msg, ackCount) -> {
            actual.set(ackCount.getCount());
            latch.countDown();
        });

        node3.send(new Message(factory,
                CountNodesMessage.TYPE));

        latch.await();
        Assert.assertEquals(3, actual.get());

    }

    private DatagramGossipNode createNode(GossipNodeShared shared) throws IOException {
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

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    private static class TestMessage implements Mutable<TestMessage>, Serializable {
        private int value;

        @Override
        public void copyFrom(TestMessage obj) {
            if (obj == null) {
                value = 0;
                return;
            }
            value = obj.value;
        }

        @Override
        public void deserialize(ByteBuffer buffer) {
            value = buffer.getInt();
        }

        @Override
        public void serialize(ByteBuffer buffer) {
            buffer.putInt(value);
        }
    }
}