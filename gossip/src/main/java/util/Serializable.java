package util;

import java.nio.ByteBuffer;

public interface Serializable {
    void deserialize(ByteBuffer buffer);

    void serialize(ByteBuffer buffer);
}
