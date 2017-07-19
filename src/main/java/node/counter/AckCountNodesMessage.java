package node.counter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.MessageType;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@Getter
@Setter
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class AckCountNodesMessage implements Mutable<AckCountNodesMessage>, Serializable {
    public static final MessageType<AckCountNodesMessage> ACK_COUNT_NODES_MESSAGE =
            new MessageType<>(
                    "ACK_COUNT_NODES_MESSAGE",
                    AckCountNodesMessage.class);

    private long count;

    @Override
    public void copyFrom(AckCountNodesMessage obj) {
        if (obj == null) {
            count = 0;
            return;
        }
        count = obj.count;
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        count = buffer.getLong();
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putLong(count);
    }
}
