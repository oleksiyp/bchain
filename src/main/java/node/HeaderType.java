package node;

import io.netty.util.collection.IntObjectHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.mutable.AbstractChoice;
import util.mutable.Mutable;
import util.mutable.MutableLong;

import java.util.Map;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HeaderType<T extends Mutable<T>> extends AbstractChoice<T> {

    public static final HeaderType<MutableLong> REFERENCE_ID = new HeaderType<>(
            0,
            "REFERENCE_ID",
            MutableLong.class,
            (f) -> new MutableLong());

    public static final Map<Integer, HeaderType<?>> SYSTEM_LEVEL = new IntObjectHashMap<>();
    static {
        register(SYSTEM_LEVEL, REFERENCE_ID);
    }


    public HeaderType(int tag, String name, Class<T> type, Function<GossipFactory, T> constructor) {
        super(tag, name, type, (obj) -> constructor.apply((GossipFactory) obj));
    }
}
