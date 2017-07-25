package util.pattern;

import lombok.ToString;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@ToString
public class PatternMatcher<T> implements Consumer<T> {
    public static final int HASH_SIZE = 16;
    public static final int LIST_SIZE = 16;

    private final Set<Function<Object, List<Key>>> scans;
    private final Map<Key, List<Consumer<T>>> matchers;

    public PatternMatcher() {
        matchers = new HashMap<>(HASH_SIZE);
        scans = new HashSet<>(HASH_SIZE);
    }

    public PatternMatcher<T> on(Key<? extends T> key, Consumer<T> handler) {
        List<Consumer<T>> handlers = matchers.computeIfAbsent(key,
                (k) -> new ArrayList<>(LIST_SIZE));
        handlers.add(handler);
        scans.add(key.scanKeysFunction());
        return this;
    }

    public <R extends T> PatternMatcher<T> on(Key<? extends T> key, Class<R> type, Consumer<R> handler) {
        List<Consumer<T>> handlers = matchers.computeIfAbsent(key,
                (k) -> new ArrayList<>(LIST_SIZE));
        handlers.add((val) -> handler.accept(type.cast(val)));
        scans.add(key.scanKeysFunction());
        return this;
    }

    @Override
    public void accept(T value) {
        for (Function<Object, List<Key>> scan : scans) {
            for (Key key : scan.apply(value)) {
                if (key.test(value)) {
                    List<Consumer<T>> list = matchers.get(key);
                    if (list != null) {
                        list.forEach(consumer -> consumer.accept(value));
                    }
                }
            }
        }
    }

    private static <T> PatternMatcher<T> matcher() {
        return new PatternMatcher<>();
    }

    public static void main(String[] args) {
//        PatternMatcher<Object> pm;
//        pm = matcher()
//                .on(ifType(String.class), System.out::println)
//                .on(ifType(Number.class), Number.class,
//                        matcher().
//                                on(ifType(Integer.class), Integer.class, x -> System.out.println("int " + (x + 3)));
//
//        pm.accept("abc");
//        pm.accept("def");
//        pm.accept(3);
    }



}
