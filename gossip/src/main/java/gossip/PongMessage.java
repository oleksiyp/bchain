package gossip;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import gossip.message.AbstractMessage;
import gossip.message.MessageType;
import gossip.registry.RegistryItem;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
    public void deserialize(DataInput in) throws IOException {
        super.deserialize(in);
        port = in.readInt();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        super.serialize(out);
        out.writeInt(port);
    }

    @Override
    public void clear() {
        super.clear();
        port = 0;
    }
}
