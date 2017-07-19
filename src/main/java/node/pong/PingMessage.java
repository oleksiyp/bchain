package node.pong;

import lombok.ToString;
import node.MessageType;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@ToString
public class PingMessage implements Mutable<PingMessage>, Serializable {
    public static final MessageType<PingMessage> TYPE = new MessageType<>(
            "PING_MESSAGE",
            PingMessage.class);

    @Override
    public void copyFrom(PingMessage obj) {
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
    }

    @Override
    public void serialize(ByteBuffer buffer) {
    }
}
