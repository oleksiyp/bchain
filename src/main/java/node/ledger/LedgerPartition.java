package node.ledger;

import io.netty.util.collection.LongObjectHashMap;

import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.abs;
import static java.util.Arrays.setAll;

public class LedgerPartition implements Iterable<LedgerActor> {
    private final LedgerActor[] ledger;
    private final Map<Long, LedgerActor> actorMap;

    private final long retentionTime;
    private long from, to;

    public LedgerPartition(int retentionSize, long retentionTime) {
        this.retentionTime = retentionTime;
        ledger = new LedgerActor[retentionSize];
        actorMap = new LongObjectHashMap<>(retentionSize);
        setAll(ledger, i -> new LedgerActor());
    }

    public LedgerActor addActor(long id, long timestamp) {
        if (abs(timestamp - System.currentTimeMillis()) > retentionTime) {
            return null;
        }


        if (actorMap.containsKey(id)) {
            return null;
        }

        if (to - from >= ledger.length) {
            removeActor(from);
            from++;
        }

        LedgerActor actor = actorAt(to++);
        actorMap.put(id, actor);
        return actor;
    }

    private int mapIndex(long seq) {
        return (int)(seq % ledger.length);
    }

    private void removeActor(long idx) {
        LedgerActor actor = actorAt(idx);
        actorMap.remove(actor.id);
        actor.clear();
    }

    @Override
    public Iterator<LedgerActor> iterator() {
        return new Iterator<LedgerActor>() {
            long idx = from;

            @Override
            public boolean hasNext() {
                return idx < to;
            }

            @Override
            public LedgerActor next() {
                return actorAt(idx++);
            }
        };
    }

    public void cleanup() {
        long time = System.currentTimeMillis();
        while (from < to){
            LedgerActor actor = actorAt(from);
            if (time - actor.headers.getTimestamp() < retentionTime) {
                break;
            }
            removeActor(from++);
        }
    }

    private LedgerActor actorAt(long idx) {
        return ledger[mapIndex(idx)];
    }

    public LedgerActor actorById(long id) {
        return actorMap.get(id);
    }

    public long size() {
        return to - from;
    }
}
