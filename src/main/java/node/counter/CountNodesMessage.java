package node.counter;

import lombok.ToString;
import node.MessageType;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@ToString
public class CountNodesMessage implements Mutable<CountNodesMessage>, Serializable {
    public static final MessageType<CountNodesMessage> COUNT_NODES_MESSAGE_MESSAGE =
            new MessageType<>(
                    "COUNT_NODES_MESSAGE",
                    CountNodesMessage.class
            );

    @Override
    public void copyFrom(CountNodesMessage obj) {
    }

    @Override
    public void deserialize(ByteBuffer buffer) {

    }

    @Override
    public void serialize(ByteBuffer buffer) {

    }
}
