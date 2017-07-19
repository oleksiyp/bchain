package node.datagram.event;

import util.mutable.ChoiceType;
import util.mutable.Mutable;

public class EventType<T extends Mutable<T>> extends ChoiceType<T> {
    public EventType(String name, Class<T> type) {
        super(name, type);
    }
}
