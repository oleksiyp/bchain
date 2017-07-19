package node;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.counter.CountNodesTypes;
import node.datagram.DatagramGossipNode;
import node.datagram.DatagramGossipNodeShared;
import node.factory.GossipFactoryImpl;
import node.ledger.ActorContext;
import node.ledger.Ledger;
import node.ledger.UberActor;
import node.pong.PongActor;
import node.pong.PongTypes;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static node.factory.GossipTypes.*;

public class PerformanceTest {

    public static final MessageType<TestMessage> TEST_MESSAGE = new MessageType<>(
            "TEST",
            TestMessage.class
    );

    public static final int N_NODES = 100;
    private static final int N_MESSAGES = 100;

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
                        .merge(0x200, PongTypes.messageRegistry())
                        .register(0x300, TEST_MESSAGE, TestMessage::new),
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

        List<CompletableFuture<?>> futures = new ArrayList<>();
        gossips = new ArrayList<>();
        for (int i = 0; i < N_NODES; i++) {
            DatagramGossipNode node = createNode(shared);
            gossips.add(node);
            if (gossips.size() <= 1) {
                continue;
            }
            int j = rnd.nextInt(gossips.size());
            futures.add(PongActor.join(node, gossips.get(j).address()));
        }


//        allOf(futures.toArray(new CompletableFuture[0]))
//                .get();
        SECONDS.sleep(5);
    }


    @Test
    public void send1Mesage() throws Exception {

        CountDownLatch latch = new CountDownLatch(N_NODES * N_MESSAGES);

        gossips.forEach(node -> {
            node.listen(TEST_MESSAGE, (msg, ackCount) -> {
                System.out.println(latch);
                latch.countDown();
            });
        });

        for (int i = 0; i < N_MESSAGES; i++) {
            int finalI = i;
            gossips.get(0).send(new Message(factory,
                    TEST_MESSAGE,
                    msg -> msg.setValue(finalI)));
        }

        latch.await();
    }

    private DatagramGossipNode createNode(DatagramGossipNodeShared shared) throws IOException {
        MappedQueue<UberActor> mappedQueue = new MappedQueue<>(
                1024,
                SECONDS.toMillis(60),
                shared.getFactory()::createUberActor,
                Mutable::clear);

        ActorContext context = new ActorContext();

        return new DatagramGossipNode(
                shared,
                Inet4Address.getByName("localhost"),
                Inet4Address.getByName("localhost"),
                portGenerator,
                idGenerator,
                new Ledger(mappedQueue, context));
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