package node.datagram.event;

import io.netty.util.collection.IntObjectHashMap;
import lombok.*;
import node.GossipFactory;
import util.mutable.AbstractChoice;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode
public class EventType<T extends Mutable<T>> extends AbstractChoice<T> {
    public static final Map<Integer, EventType<?>> SYSTEM_LEVEL = new IntObjectHashMap<>();

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
        register(SYSTEM_LEVEL, READ_EVENT);
        register(SYSTEM_LEVEL, WRITE_EVENT);
        register(SYSTEM_LEVEL, SEND_EVENT);
        register(SYSTEM_LEVEL, REGISTER_LISTENER_EVENT);
        register(SYSTEM_LEVEL, REGISTER_PARTY_EVENT);
    }

    protected EventType(int tag,
                        String name,
                        Class<T> type,
                        Function<GossipFactory, T> constructor) {
        super(tag, name, type, (obj) -> constructor.apply((GossipFactory) obj));
    }
}
