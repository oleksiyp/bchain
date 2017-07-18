package node.pong;

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
public class PongMessage implements Mutable<PongMessage>, Serializable {
    public static final MessageType<PongMessage> TYPE =
            new MessageType<>(
                    62,
                    "PONG_MESSAGE",
                    PongMessage.class,
                    PongMessage::new);

    @Override
    public void copyFrom(PongMessage obj) {
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
    }

    @Override
    public void serialize(ByteBuffer buffer) {
    }
}
