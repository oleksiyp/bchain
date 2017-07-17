package node.datagram;

import io.netty.util.collection.IntObjectHashMap;
import util.mutable.AbstractChoice;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageType<T extends Mutable<T>> extends AbstractChoice<T> {
    public static final MessageType<PingMessage> PING_MESSAGE_TYPE = new MessageType<>(
            0,
            "PING_MESSAGE",
            PingMessage.class,
            PingMessage::new);

    public static final Map<Integer, MessageType<?>> SYSTEM_LEVEL = new IntObjectHashMap<>();
    static {
        register(SYSTEM_LEVEL, PING_MESSAGE_TYPE);
    }

    public MessageType(int tag, String name, Class<T> type, Function<GossipFactory, T> constructor) {
        super(tag, name, type, obj -> constructor.apply((GossipFactory) obj));
    }

    public MessageType(int tag, String name, Class<T> type, Supplier<T> constructor) {
        super(tag, name, type, obj -> constructor.get());
    }
}
