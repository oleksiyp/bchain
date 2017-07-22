package node.pong;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.MessageType;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@ToString
@Getter
@Setter
public class PingMessage implements Mutable<PingMessage>, Serializable {
    public static final MessageType<PingMessage> TYPE = new MessageType<>(
            "PING_MESSAGE",
            PingMessage.class);

    int value;

    @Override
    public void copyFrom(PingMessage obj) {
        if (obj == null) {
            value = 0;
            return;
        }
        value = obj.value;
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        value = buffer.getInt();
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(value);
    }
}
