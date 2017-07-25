package node2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node2.in_out.*;
import node2.message.AbstractMessage;
import node2.message.MessageType;
import node2.registry.RegistryItem;

@Getter
@Setter
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class PongMessage extends AbstractMessage {
    public static final MessageType<PongMessage> TYPE =
            new MessageType<>(
                    "PONG_MESSAGE",
                    PongMessage.class);

    int port;

    @Override
    public RegistryItem<?> getType() {
        return TYPE;
    }

    @Override
    public void deserialize(In<?> in) {
        super.deserialize(in);
        port = in.getInt();
    }

    @Override
    public void serialize(Out<?> out) {
        super.serialize(out);
        out.putInt(port);
    }

    @Override
    public void clear() {
        super.clear();
        port = 0;
    }
}
