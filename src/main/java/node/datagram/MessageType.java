package node.datagram;

import io.netty.util.collection.IntObjectHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Supplier;

@Getter
@Setter
@EqualsAndHashCode
public class MessageType<T extends Mutable<T>> implements Choice<T> {
    private final int tag;
    private final String name;
    private final Class<T> type;
    private final Supplier<T> constructor;

    public static final Map<Integer, MessageType<?>> ALL = new IntObjectHashMap<>();
    public static final MessageType<PingMessage> PING_MESSAGE_TYPE = register(0,
            "PING_MESSAGE",
            PingMessage.class,
            PingMessage::new);

    public static <T extends Mutable<T>>
    MessageType<T> register(
            int tag,
            String name,
            Class<T> clazz,
            Supplier<T> constructor) {
        MessageType<T> headerName = new MessageType<>(tag, name, clazz, constructor);
        if (ALL.put(tag, headerName) != null) {
            throw new RuntimeException("Tag " + tag + " already exist: " + headerName);
        }
        return headerName;
    }

    private MessageType(int tag, String name, Class<T> type, Supplier<T> constructor) {
        this.tag = tag;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
    }

    public int getTag() {
        return tag;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
