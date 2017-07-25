package gossip;

import io.netty.util.collection.LongObjectHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Math.abs;
import static java.util.Arrays.setAll;

public class LedgerImpl<T> implements Iterable<T> {
    private final long[] ids;
    private final long[] timestamps;
    private final List<T> queue;
    private final Map<Long, T> map;
    private final Consumer<T> wiper;

    private final long maxTime;
    private long from, to;

    public LedgerImpl(int maxSize,
                      long maxTime,
                      Consumer<T> wiper) {
        this.maxTime = maxTime;
        queue = new ArrayList<>(maxSize);
        ids = new long[maxSize];
        timestamps = new long[maxSize];
        map = new LongObjectHashMap<>(maxSize);
        this.wiper = wiper;
        for (int i = 0; i < maxSize; i++) {
            queue.add(null);
        }
    }

    public boolean add(long id, long timestamp, T item) {
        long time = System.currentTimeMillis();
        if (abs(timestamp - time) > maxTime) {
            return false;
        }

        if (map.containsKey(id)) {
            return false;
        }

        cleanupByTime(time);

        if (to - from >= queue.size()) {
            removeItem(from);
            from++;
        }


        ids[mapIndex(to)] = id;
        timestamps[mapIndex(to)] = timestamp;
        queue.set(mapIndex(to), item);
        map.put(id, item);

        to++;

        return true;
    }

    private void removeItem(long idx) {
        T item = itemAt(idx);
        map.remove(idAt(idx));

        ids[mapIndex(idx)] = 0;
        timestamps[mapIndex(idx)] = 0;
        queue.set(mapIndex(idx), null);

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
        return (int)(seq % queue.size());
    }

    private T itemAt(long idx) {
        return (T) queue.get(mapIndex(idx));
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
