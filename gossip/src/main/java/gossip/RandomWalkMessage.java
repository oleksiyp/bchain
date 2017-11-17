package gossip;

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
    public void deserialize(DataInput in) throws IOException {
        super.deserialize(in);
        hops = in.readInt();
        t = in.readLong();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        super.serialize(out);
        out.writeInt(hops);
        out.writeLong(t);
    }

    @Override
    public void clear() {
        super.clear();
        hops = 0;
        t = 0;
    }

}
