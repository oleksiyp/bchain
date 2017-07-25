package gossip.in_out;

import gossip.registry.RegistryItem;

public interface Serializable {
    RegistryItem<?> getType();

    void deserialize(In<?> in);

    void serialize(Out<?> out);
}
