package node2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node2.in_out.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class PongMessage implements Serializable {
    public static final MessageType<PongMessage> TYPE =
            new MessageType<>(
                    "PONG_MESSAGE",
                    PongMessage.class);

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
