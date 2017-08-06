package gossip.registry;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gossip.Clearable;
import gossip.TypeAware;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistryMapping<T extends RegistryItem<C>, C> {
    private final TIntIntMap tagToIdxs;
    private final int [] idxToTags;
    int [][] tagIdxCache;

    private final List<Supplier<C>> constructors;
    private final List<Pool<C>> pools;
    private final List<T> choiceTypeList;
    private final Map<Integer, T> choiceTypeMap;
    private final Map<T, Integer> reverseChoiceTypeMap;
    private int nextCacheItem;

    public RegistryMapping(Map<Integer, RegistryItem<?>> choiceTypeMap,
                           Map<Integer, Supplier<?>> constructors,
                           Class<T> clazz) {
        tagToIdxs = new TIntIntHashMap(choiceTypeMap.size());
        int[] idxToTags = new int[choiceTypeMap.size()];

        this.constructors = new ArrayList<>();

        this.choiceTypeList = new ArrayList<>();
        this.choiceTypeMap = new HashMap<>(choiceTypeMap.size());
        reverseChoiceTypeMap = new IdentityHashMap<>();

        int i = 0;
        pools = new ArrayList<>();

        for (int tag : choiceTypeMap.keySet()) {
            RegistryItem<?> registryItemObj = choiceTypeMap.get(tag);
            if (clazz.isInstance(registryItemObj)) {
                T choiceType = clazz.cast(registryItemObj);

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

    public <C extends RegistryItem<?>> int tagByChoiceType(C choice) {
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

    public <R extends C> Supplier<R> constructorByChoice(RegistryItem<R> type) {
        int tag = tagByChoiceType(type);
        if (tag == -1) {
            throw new IllegalArgumentException(type.toString());
        }
        return (Supplier<R>) constructorByIdx(idxByTag(tag));
    }

    public <R extends C> Consumer<R> poolByChoice(RegistryItem<R> type) {
        int tag = tagByChoiceType(type);
        if (tag == -1) {
            throw new IllegalArgumentException(type.toString());
        }
        return (Consumer<R>) pools.get(idxByTag(tag));
    }

    public <R extends C> R create(RegistryItem<R> type) {
        return constructorByChoice(type).get();
    }

    public <R extends C> void reuse(RegistryItem<R> type, R value) {
        poolByChoice(type).accept(value);
    }

    public void reuse(TypeAware typeAware) {
        poolByChoice((RegistryItem<C>) typeAware.getType()).accept((C) typeAware);
    }

}
