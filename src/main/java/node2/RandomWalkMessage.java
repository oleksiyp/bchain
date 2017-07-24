package node2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node2.in_out.*;

@Getter
@Setter
@ToString
public class RandomWalkMessage extends AbstractMessage {
    public static final MessageType<RandomWalkMessage> TYPE =
            new MessageType<>("RANDOM_WALK_MESSAGE", RandomWalkMessage.class);

    long t;
    int hops;

    @Override
    public ChoiceType<?> getType() {
        return TYPE;
    }

    @Override
    public void deserialize(In<?> in) {
        super.deserialize(in);
        hops = in.getInt();
        t = in.getLong();
    }

    @Override
    public void serialize(Out<?> out) {
        super.serialize(out);
        out.putInt(hops);
        out.putLong(t);
    }

    @Override
    public void clear() {
        super.clear();
        hops = 0;
        t = 0;
    }

}
