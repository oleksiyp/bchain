package node2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node2.in_out.*;
import node2.message.AbstractMessage;
import node2.message.MessageType;
import node2.registry.RegistryItem;

@ToString
@Getter
@Setter
public class PingMessage extends AbstractMessage {
    public static final MessageType<PingMessage> TYPE = new MessageType<>(
            "PING_MESSAGE",
            PingMessage.class);

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
