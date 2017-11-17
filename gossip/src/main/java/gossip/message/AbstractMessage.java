package gossip.message;

import lombok.Getter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

public abstract class AbstractMessage implements Message {
    public static final Random RNG = new Random();

    @Getter
    private long id;

    public void generateId() {
        id = RNG.nextLong();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeLong(id);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        id = in.readLong();
    }

    @Override
    public void clear() {
        id = 0;
    }
}
