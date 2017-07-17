package util.mutable;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;

@Getter
@EqualsAndHashCode
public abstract class AbstractChoice<T extends Mutable<T>> implements Choice<T> {
    private final int tag;
    private final String name;
    private final Class<T> type;
    private final Function<Object, T> constructor;

    public static <R extends Choice<?>> R register(
            Map<Integer, R> map,
            R choice) {

        int tag = choice.getTag();
        if (map.containsKey(tag)) {
            throw new RuntimeException("Tag " + tag + " already exist: " + choice);
        }

        if (map.put(tag, choice) != null) {
            throw new RuntimeException("Tag " + tag + " already exist: " + choice);
        }

        return choice;
    }

    protected AbstractChoice(
            int tag,
            String name,
            Class<T> type,
            Function<Object, T> constructor) {

        this.tag = tag;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
    }

    public String toString() {
        return this.getName();
    }
}
