package gossip.message;

import gossip.in_out.Serializable;
import gossip.registry.RegistryItem;

public class MessageType<T extends Serializable> extends RegistryItem<T> {
    public MessageType(String name, Class<T> type) {
        super(name, type);
    }
}
