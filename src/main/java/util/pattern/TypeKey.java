package util.pattern;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
@ToString
public class TypeKey<T> extends Key<T> {
    private static final Function<Object, List<Key>> SCAN =
            obj -> singletonList(new TypeKey<>(obj.getClass()));

    private final Class<T> type;

    public Function<Object, List<Key>> scanKeysFunction() {
        return SCAN;
    }

    @Override
    public boolean test(T t) {
        return type.isInstance(t);
    }

    public static <T> TypeKey<T> ifType(Class<T> type) {
        return new TypeKey<>(type);
    }
}
