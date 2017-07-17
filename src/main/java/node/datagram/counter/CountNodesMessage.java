package node.datagram.counter;

import lombok.ToString;
import node.datagram.MessageType;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@ToString
public class CountNodesMessage implements Mutable<CountNodesMessage>, Serializable {
    public static final MessageType<CountNodesMessage> TYPE =
            new MessageType<>(
                    60,
                    "COUNT_NODES_MESSAGE",
                    CountNodesMessage.class,
                    CountNodesMessage::new);

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
