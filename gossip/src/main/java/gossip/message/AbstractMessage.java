package gossip.message;

import lombok.Getter;
import gossip.in_out.In;
import gossip.in_out.Out;

import java.util.Random;

public abstract class AbstractMessage implements Message {
    public static final Random RNG = new Random();

    @Getter
    private long id;

    public void generateId() {
        id = RNG.nextLong();
    }

    @Override
    public void serialize(Out<?> out) {
        out.putLong(id);
    }

    @Override
    public void deserialize(In<?> in) {
        id = in.getLong();
    }

    @Override
    public void clear() {
        id = 0;
    }
}
