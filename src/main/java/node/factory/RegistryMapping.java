package node.factory;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import io.netty.util.collection.IntObjectHashMap;
import util.mutable.ChoiceType;
import util.mutable.Mutable;

import java.util.*;
import java.util.function.Supplier;

public class RegistryMapping<T extends ChoiceType<?>> {
    private final TIntIntMap tagToIdxs;
    private final int [] idxToTags;

    private final List<Supplier<Mutable<?>>> constructors;
    private final List<T> choiceTypeList;
    private final Map<Integer, T> choiceTypeMap;
    private final Map<T, Integer> reverseChoiceTypeMap;

    public RegistryMapping(Map<Integer, ChoiceType<?>> choiceTypeMap,
                           Map<Integer, Supplier<Mutable<?>>> constructors,
                           Class<T> clazz) {
        tagToIdxs = new TIntIntHashMap(choiceTypeMap.size());
        int[] idxToTags = new int[choiceTypeMap.size()];

        this.constructors = new ArrayList<>();

        this.choiceTypeList = new ArrayList<>();
        this.choiceTypeMap = new IntObjectHashMap<>(choiceTypeMap.size());
        reverseChoiceTypeMap = new IdentityHashMap<>();

        int i = 0;
        for (int tag : choiceTypeMap.keySet()) {
            ChoiceType<?> choiceTypeObj = choiceTypeMap.get(tag);
            if (clazz.isInstance(choiceTypeObj)) {
                T choiceType = clazz.cast(choiceTypeObj);

                tagToIdxs.put(tag, i);
                idxToTags[i] = tag;

                this.constructors.add(constructors.get(tag));

                this.choiceTypeList.add(choiceType);
                this.choiceTypeMap.put(tag, choiceType);
                this.reverseChoiceTypeMap.put(choiceType, tag);

                i++;
            }
        }
        this.idxToTags = Arrays.copyOf(idxToTags, i);
    }




    public int nElements() {
        return idxToTags.length;
    }

    public <C extends ChoiceType<?>> int tagByChoiceType(C choice) {
        return reverseChoiceTypeMap.getOrDefault(choice, -1);
    }

    public int idxByTag(int tag) {
        if (!tagToIdxs.containsKey(tag)) {
            return -1;
        }
        return tagToIdxs.get(tag);
    }

    public T choiceTypeByIdx(int idx) {
        return choiceTypeList.get(idx);
    }

    public int tagByIdx(int idx) {
        return idxToTags[idx];
    }

    public Supplier<Mutable<?>> getConstructor(int idx) {
        return constructors.get(idx);
    }
}
