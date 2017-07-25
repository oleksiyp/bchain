package node2.message;

import node2.in_out.Serializable;
import node2.registry.RegistryItem;

public class MessageType<T extends Serializable> extends RegistryItem<T> {
    public MessageType(String name, Class<T> type) {
        super(name, type);
    }
}
