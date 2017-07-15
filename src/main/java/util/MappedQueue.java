package util;

import io.netty.util.collection.LongObjectHashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Math.abs;
import static java.util.Arrays.setAll;

public class MappedQueue<T> implements Iterable<T> {
    private final long[] ids;
    private final long[] timestamps;
    private final Object[] queue;
    private final Map<Long, T> map;
    private final Consumer<T> wiper;

    private final long maxTime;
    private long from, to;

    public MappedQueue(int maxSize, long maxTime, Supplier<T> factory, Consumer<T> wiper) {
        this.maxTime = maxTime;
        queue = new Object[maxSize];
        ids = new long[maxSize];
        timestamps = new long[maxSize];
        map = new LongObjectHashMap<>(maxSize);
        this.wiper = wiper;
        setAll(queue, i -> factory.get());
    }

    public T add(long id, long timestamp) {
        long time = System.currentTimeMillis();
        if (abs(timestamp - time) > maxTime) {
            return null;
        }

        if (map.containsKey(id)) {
            return null;
        }

        cleanupByTime(time);

        if (to - from >= queue.length) {
            removeItem(from);
            from++;
        }

        T item = itemAt(to);

        ids[mapIndex(to)] = id;
        timestamps[mapIndex(to)] = timestamp;
        map.put(id, item);

        to++;

        return item;
    }

    private void removeItem(long idx) {
        T item = itemAt(idx);
        map.remove(idAt(idx));

        ids[mapIndex(idx)] = 0;
        timestamps[mapIndex(idx)] = 0;

        wiper.accept(item);
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            long idx = from;

            @Override
            public boolean hasNext() {
                return idx < to;
            }

            @Override
            public T next() {
                return itemAt(idx++);
            }
        };
    }

    private void cleanupByTime(long time) {
        while (from < to){
            long ts = timestampAt(from);
            if (time - ts < maxTime) {
                break;
            }
            removeItem(from++);
        }
    }

    private int mapIndex(long seq) {
        return (int)(seq % queue.length);
    }

    private T itemAt(long idx) {
        return (T) queue[mapIndex(idx)];
    }

    private long idAt(long idx) {
        return ids[mapIndex(idx)];
    }

    private long timestampAt(long idx) {
        return timestamps[mapIndex(idx)];
    }

    public T get(long id) {
        return map.get(id);
    }

    public long size() {
        return to - from;
    }
}
