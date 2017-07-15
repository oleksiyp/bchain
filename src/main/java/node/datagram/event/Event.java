package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.Message;
import node.datagram.Party;
import node.datagram.shared.GossipNodeShared;
import util.mutable.Mutable;
import util.mutable.MutableUnion;

import java.util.function.Supplier;

@Setter
@Getter
@ToString(exclude = {"shared", "self"}, includeFieldNames = false)
public class Event implements Mutable<Event> {
    private Party self;
    private GossipNodeShared shared;

    private final MutableUnion<EventType<?>> subEvent;

    public Event(Supplier<Message> messageFactory) {
        subEvent = new MutableUnion<>(EventType.ALL);
    }

    @Override
    public void copyFrom(Event event) {
        if (event == null) {
            self = null;
            subEvent.copyFrom(null);
            return;
        }
        self = event.self;
        subEvent.copyFrom(event.getSubEvent());
    }

    public boolean isSubEventActive(EventType<?> eventType) {
        return getSubEvent().isActive(eventType);
    }

    public <T extends Mutable<T>> T getSubEvent(EventType<T> eventType) {
        return getSubEvent().get(eventType);
    }

    public <T extends Mutable<T>> T activateSubEvent(EventType<T> eventType) {
        return getSubEvent().activate(eventType);
    }
}
