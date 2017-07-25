package node2.in_out;

import node2.registry.RegistryItem;

public interface Serializable {
    RegistryItem<?> getType();

    void deserialize(In<?> in);

    void serialize(Out<?> out);
}
