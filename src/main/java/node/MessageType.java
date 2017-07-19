package node;

import util.mutable.ChoiceType;
import util.mutable.Mutable;

public class MessageType<T extends Mutable<T>> extends ChoiceType<T> {
    public MessageType(String name, Class<T> type) {
        super(name, type);
    }
}
