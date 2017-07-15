package node.datagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.shared.GossipNodeShared;
import org.junit.Before;
import org.junit.Test;
import util.Serializable;
import util.mutable.Mutable;

import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static node.datagram.MessageType.PING_MESSAGE_TYPE;

public class DatagramGossipNodeTest {

    private Gossip node1;
    private Gossip node2;
    private Gossip node3;

    public static final MessageType<TestMessage> TEST_MESSAGE = MessageType.register(1,
            "TEST",
            TestMessage.class,
            TestMessage::new);

    @Before
    public void setUp() throws Exception {
        Supplier<Message> messageFactory = () -> {
                return new Message();
        };

        GossipNodeShared shared = new GossipNodeShared(
                16 * 1024,
                1024,
                1024,
                messageFactory);

        Random rnd = new Random();

        node1 = new DatagramGossipNode(
                shared,
                Inet4Address.getByName("localhost"),
                Inet4Address.getByName("localhost"),
                () -> rnd.nextInt(65536),
                1024,
                SECONDS.toMillis(60),
                messageFactory);

        node2 = new DatagramGossipNode(
                shared,
                Inet4Address.getByName("localhost"),
                Inet4Address.getByName("localhost"),
                () -> rnd.nextInt(65536),
                1024,
                SECONDS.toMillis(60),
                messageFactory);

        node3 = new DatagramGossipNode(
                shared,
                Inet4Address.getByName("localhost"),
                Inet4Address.getByName("localhost"),
                () -> rnd.nextInt(65536),
                1024,
                SECONDS.toMillis(60),
                messageFactory);

        long []start = new long[1];
        node2.listen(TEST_MESSAGE, msg -> {
            if (start[0] == 0) {
                start[0] = System.nanoTime();
            }
            TestMessage testMessage = msg.getSubType(TEST_MESSAGE);
            testMessage.value++;
            double t = (System.nanoTime() - start[0]) / 1e9;
            if (t > 1000) {
                System.out.printf("%06.3f: %d%n", t, testMessage.getValue());
            }
        });

        node1.join(node2.address());
        node3.join(node2.address());
        node3.join(node1.address());
    }

    @Test
    public void send1Mesage() throws Exception {
//        Message msg = new Message();
//        TestMessage testMessage = msg.getSubType().activate(TEST_MESSAGE);
//        testMessage.value = 22;
//        node1.send(msg);

        SECONDS.sleep(2);

        System.out.println(((GossipNode)node1).getRemoteParties());
        System.out.println(((GossipNode)node2).getRemoteParties());
        System.out.println(((GossipNode)node3).getRemoteParties());

        Message msg = new Message();
        TestMessage testMessage = msg.getSubType().activate(TEST_MESSAGE);
        testMessage.value = 0;
        node3.send(msg);

        SECONDS.sleep(6000);
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