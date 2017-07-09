package util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class VolatileMap<K, V> {
    private final Function<Map<K, V>, Map<K, V>> constructor;
    private final Map<K, V> passive;
    private volatile Map<K, V> active;

    public VolatileMap(Function<Map<K, V>, Map<K, V>> constructor) {
        passive = constructor.apply(Collections.emptyMap());
        active = constructor.apply(Collections.emptyMap());
        this.constructor = constructor;
    }

    public void modify(Consumer<Map<K, V>> handler) {
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

    public void put(K key, V value) {
        modify(col -> col.put(key, value));
    }

    public void remove(K key) {
        modify(col -> col.remove(key));
    }

    public void clear() {
        modify(Map::clear);
    }

    public V get(Object key) {
        return active.get(key);
    }

    public void forEach(BiConsumer<K, V> action) {
        active.forEach(action);
    }

    public Set<K> keySet() {
        return active.keySet();
    }

    public Collection<V> values() {
        return active.values();
    }
}

