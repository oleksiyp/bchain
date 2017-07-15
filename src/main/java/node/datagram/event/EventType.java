package node.datagram.event;

import io.netty.util.collection.IntObjectHashMap;
import lombok.*;
import node.datagram.Choice;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Supplier;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class EventType<T extends Mutable<T>> implements Choice<T> {
    public static final Map<Integer, EventType<?>> ALL = new IntObjectHashMap<>();
    public static final EventType<ReadEvent> READ_EVENT = new EventType<>(
            0,
            "READ_EVENT",
            ReadEvent.class,
            ReadEvent::new);

    public static final EventType<WriteEvent> WRITE_EVENT = new EventType<>(
            1,
            "WRITE_EVENT",
            WriteEvent.class,
            WriteEvent::new);

    public static final EventType<SendEvent> SEND_EVENT = new EventType<>(
            2,
            "SEND_EVENT",
            SendEvent.class,
            SendEvent::new);

    public static final EventType<RegisterListenerEvent> REGISTER_LISTENER_EVENT = new EventType<>(
            3,
            "REGISTER_LISTENER_EVENT",
            RegisterListenerEvent.class,
            RegisterListenerEvent::new);

    public static final EventType<RegisterPartyEvent> REGISTER_PARTY_EVENT = new EventType<>(
            4,
            "REGISTER_PARTY_EVENT",
            RegisterPartyEvent.class,
            RegisterPartyEvent::new);

    static {
        ALL.put(READ_EVENT.getTag(), READ_EVENT);
        ALL.put(WRITE_EVENT.getTag(), WRITE_EVENT);
        ALL.put(SEND_EVENT.getTag(), SEND_EVENT);
        ALL.put(REGISTER_LISTENER_EVENT.getTag(), REGISTER_LISTENER_EVENT);
        ALL.put(REGISTER_PARTY_EVENT.getTag(), REGISTER_PARTY_EVENT);
    }

    private final int tag;
    private final String name;
    private final Class<T> type;
    private final Supplier<T> constructor;

    @Override
    public String toString() {
        return name;
    }
}
