package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import node.Party;
import node.factory.GossipFactory;
import node.datagram.SocketGossipNodeShared;
import util.mutable.Mutable;
import util.mutable.MutableUnion;

@Setter
@Getter
public class Event implements Mutable<Event> {
    private Party self;
    private SocketGossipNodeShared shared;

    private final MutableUnion<EventType<?>> subEvent;

    public Event(GossipFactory factory) {
        subEvent = new MutableUnion<>(factory.getEventTypes());
    }

    @Override
    public void copyFrom(Event event) {
        if (event == null) {
            self = null;
            subEvent.clear();
            return;
        }
        self = event.self;
        subEvent.copyFrom(event.getSubEvent());
    }

    public boolean isSubEventActive(EventType<?> eventType) {
        return getSubEvent().instanceOf(eventType);
    }

    public <T extends Mutable<T>> T getSubEvent(EventType<T> eventType) {
        return getSubEvent().castTo(eventType);
    }

    public <T extends Mutable<T>> T activateSubEvent(EventType<T> eventType) {
        return getSubEvent().activate(eventType);
    }

    @Override
    public String toString() {
        return subEvent.toString();
    }
}
