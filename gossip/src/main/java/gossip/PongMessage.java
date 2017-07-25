package gossip;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import gossip.in_out.*;
import gossip.message.AbstractMessage;
import gossip.message.MessageType;
import gossip.registry.RegistryItem;

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
