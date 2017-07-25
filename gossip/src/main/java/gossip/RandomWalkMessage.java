package gossip;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import gossip.in_out.*;
import gossip.message.AbstractMessage;
import gossip.message.MessageType;
import gossip.registry.RegistryItem;

@Getter
@Setter
@ToString
public class RandomWalkMessage extends AbstractMessage {
    public static final MessageType<RandomWalkMessage> TYPE =
            new MessageType<>("RANDOM_WALK_MESSAGE", RandomWalkMessage.class);

    long t;
    int hops;

    @Override
    public RegistryItem<?> getType() {
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
