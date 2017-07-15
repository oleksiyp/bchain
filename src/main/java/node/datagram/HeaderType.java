package node.datagram;

import io.netty.util.collection.IntObjectHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Supplier;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HeaderType<T extends Mutable<T>> implements Choice<T> {
    private final int tag;
    private final String name;
    private final Class<T> type;
    private final Supplier<T> constructor;

    public static final Map<Integer, HeaderType<?>> ALL = new IntObjectHashMap<>();

    public static <T extends Mutable<T>> HeaderType<T> register(
            int tag,
            String id,
            Class<T> clazz,
            Supplier<T> factory) {

        HeaderType<T> type = new HeaderType<>(tag, id, clazz, factory);
        if (ALL.put(tag, type) != null) {
            throw new RuntimeException("Tag " + tag + " already exist: " + type);
        }
        return type;
    }

    private HeaderType(int tag, String name, Class<T> type, Supplier<T> constructor) {
        this.tag = tag;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
    }

}
