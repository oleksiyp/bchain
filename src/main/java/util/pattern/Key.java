package util.pattern;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

@EqualsAndHashCode
public abstract class Key<T> implements Predicate<T> {
    public static final Key<?> ANY = new AnyKey();

    public abstract Function<Object, List<Key>> scanKeysFunction();

    @EqualsAndHashCode
    private static class AnyKey extends Key<Object> {
        public static final Function<Object, List<Key>> SCAN =
                (val) -> singletonList(ANY);

        @Override
        public Function<Object, List<Key>> scanKeysFunction() {
            return SCAN;
        }

        @Override
        public boolean test(Object o) {
            return false;
        }
    }
}
