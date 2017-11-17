package gossip.in_out;

import gossip.registry.RegistryItem;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface Serializable {
    RegistryItem<?> getType();

    void deserialize(DataInput in) throws IOException;

    void serialize(DataOutput out) throws IOException;
}
