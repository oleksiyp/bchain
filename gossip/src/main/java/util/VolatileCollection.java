package util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class VolatileCollection<T> implements Iterable<T> {
    private final Function<Collection<T>, Collection<T>> constructor;
    private final Collection<T> passive;
    private volatile Collection<T> active;

    public VolatileCollection(Function<Collection<T>, Collection<T>> constructor) {
        passive = constructor.apply(Collections.emptySet());
        active = constructor.apply(Collections.emptySet());
        this.constructor = constructor;
    }

    public void modify(Consumer<Collection<T>> handler) {
        synchronized (passive) {
            handler.accept(passive);
            active = constructor.apply(passive);
        }
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public int size() {
        return active.size();
    }

    public void add(T element) {
        modify(col -> col.add(element));
    }

    public void remove(T element) {
        modify(col -> col.remove(element));
    }

    public void clear() {
        modify(Collection::clear);
    }

    @Override
    public Iterator<T> iterator() {
        return active.iterator();
    }

    public void forEach(Consumer<? super T> action) {
        active.forEach(action);
    }

    public Stream<T> stream() {
        return active.stream();
    }
}
