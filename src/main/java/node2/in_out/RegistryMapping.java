package node2.in_out;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import io.netty.util.collection.IntObjectHashMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistryMapping<T extends ChoiceType<C>, C> {
    private final TIntIntMap tagToIdxs;
    private final int [] idxToTags;
    int [][] tagIdxCache;

    private final List<Supplier<C>> constructors;
    private final List<Pool<C>> pools;
    private final List<T> choiceTypeList;
    private final Map<Integer, T> choiceTypeMap;
    private final Map<T, Integer> reverseChoiceTypeMap;
    private int nextCacheItem;

    public RegistryMapping(Map<Integer, ChoiceType<?>> choiceTypeMap,
                           Map<Integer, Supplier<?>> constructors,
                           Class<T> clazz) {
        tagToIdxs = new TIntIntHashMap(choiceTypeMap.size());
        int[] idxToTags = new int[choiceTypeMap.size()];

        this.constructors = new ArrayList<>();

        this.choiceTypeList = new ArrayList<>();
        this.choiceTypeMap = new IntObjectHashMap<>(choiceTypeMap.size());
        reverseChoiceTypeMap = new IdentityHashMap<>();

        int i = 0;
        pools = new ArrayList<>();

        for (int tag : choiceTypeMap.keySet()) {
            ChoiceType<?> choiceTypeObj = choiceTypeMap.get(tag);
            if (clazz.isInstance(choiceTypeObj)) {
                T choiceType = clazz.cast(choiceTypeObj);

                tagToIdxs.put(tag, i);
                idxToTags[i] = tag;

                Pool<C> pool = new Pool<>(1024);
                pools.add(pool);

                Supplier<?> constructor = constructors.get(tag);

                this.constructors.add(() -> {
                    C val = pool.get();
                    if (val != null) {
                        if (val instanceof Clearable) {
                            ((Clearable) val).clear();
                        }
                        return val;
                    }

                    return (C) constructor.get();
                });

                this.choiceTypeList.add(choiceType);
                this.choiceTypeMap.put(tag, choiceType);
                this.reverseChoiceTypeMap.put(choiceType, tag);

                i++;
            }
        }
        this.idxToTags = Arrays.copyOf(idxToTags, i);

        tagIdxCache = new int[8][2];
        for (int j = 0; j < tagIdxCache.length; j++) {
            tagIdxCache[j][1] = -1;
        }
        nextCacheItem = 0;
    }




    public int nElements() {
        return idxToTags.length;
    }

    public <C extends ChoiceType<?>> int tagByChoiceType(C choice) {
        return reverseChoiceTypeMap.getOrDefault(choice, -1);
    }

    public int idxByTag(int tag) {
        for (int j = 0; j < tagIdxCache.length; j++) {
            if (tagIdxCache[j][1] != -1 && tagIdxCache[j][0] == tag) {
                return tagIdxCache[j][1];
            }
        }

        if (!tagToIdxs.containsKey(tag)) {
            return -1;
        }
        int ret = tagToIdxs.get(tag);
        if (++nextCacheItem == tagIdxCache.length) {
            nextCacheItem = 0;
        }

        tagIdxCache[nextCacheItem][0] = ret;
        tagIdxCache[nextCacheItem][1] = tag;

        return ret;
    }

    public T choiceTypeByIdx(int idx) {
        return choiceTypeList.get(idx);
    }

    public int tagByIdx(int idx) {
        return idxToTags[idx];
    }

    public Supplier<C> constructorByIdx(int idx) {
        return constructors.get(idx);
    }

    public <R extends C> Supplier<R> constructorByChoice(ChoiceType<R> type) {
        int tag = tagByChoiceType(type);
        if (tag == -1) {
            throw new IllegalArgumentException(type.toString());
        }
        return (Supplier<R>) constructorByIdx(idxByTag(tag));
    }

    public <R extends C> Consumer<R> poolByChoice(ChoiceType<R> type) {
        int tag = tagByChoiceType(type);
        if (tag == -1) {
            throw new IllegalArgumentException(type.toString());
        }
        return (Consumer<R>) pools.get(idxByTag(tag));
    }

    public <R extends C> R create(ChoiceType<R> type) {
        return constructorByChoice(type).get();
    }

    public <R extends C> void reuse(ChoiceType<R> type, R value) {
        poolByChoice(type).accept(value);
    }
}
