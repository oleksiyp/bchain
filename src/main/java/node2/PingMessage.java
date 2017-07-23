package node2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node2.in_out.*;

@ToString
@Getter
@Setter
public class PingMessage implements Serializable {
    public static final MessageType<PingMessage> TYPE = new MessageType<>(
            "PING_MESSAGE",
            PingMessage.class);

    int port;

    @Override
    public ChoiceType<?> getType() {
        return TYPE;
    }

    @Override
    public void deserialize(In<?> in) {
        port = in.getInt();
    }

    @Override
    public void serialize(Out<?> out) {
        out.putInt(port);
    }
}
