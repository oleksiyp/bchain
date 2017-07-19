package util.mutable;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lombok.ToString;
import node.factory.Registry;
import node.factory.RegistryMapping;
import util.Serializable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ToString(of = "values", includeFieldNames = false)
public class MutableSet<T extends ChoiceType<?>> implements Mutable<MutableSet<T>>, Serializable {
    private TIntSet selected;
    private final List<Mutable<?>> values;
    private RegistryMapping<T> mapping;

    public MutableSet(RegistryMapping<T> mapping) {
        this.mapping = mapping;
        selected = new TIntHashSet(mapping.nElements());
        values = new ArrayList<>(mapping.nElements());
        for (int i = 0; i < mapping.nElements(); i++) {
            values.add(mapping.getConstructor(i).get());
        }
    }

    @Override
    public void copyFrom(MutableSet<T> obj) {
        deactivateAll();

        if (obj == null) {
            return;
        }

        if (obj.selected.isEmpty()) {
            return;
        }

        TIntIterator iterator = obj.selected.iterator();
        while (iterator.hasNext()) {
            int objIdx = iterator.next();
            T choice = obj.mapping.choiceTypeByIdx(objIdx);
            int tag = mapping.tagByChoiceType(choice);
            if (tag == -1) {
                throw new RuntimeException(choice + " not registered in " + mapping);
            }
            int idx = mapping.idxByTag(tag);
            values.get(idx).copyFromObj(obj.values.get(objIdx));
            selected.add(idx);
        }
    }

    public <C extends ChoiceType<R>, R extends Mutable<R>> R activate(C choice) {
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            throw new RuntimeException(choice + " not registered in " + mapping);
        }
        int idx = mapping.idxByTag(tag);
        this.selected.add(idx);
        return (R) values.get(idx);
    }

    public boolean isActive(T choice) {
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            return false;
        }
        int idx = mapping.idxByTag(tag);
        return selected.contains(idx);
    }

    public <C extends ChoiceType<R>, R extends Mutable<R>> R get(C choice) {
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            throw new RuntimeException(choice + " not registered in " + mapping);
        }
        int idx = mapping.idxByTag(tag);
        return (R) values.get(idx);
    }

    public void deactivate(T choice) {
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            throw new RuntimeException(choice + " not registered in " + mapping);
        }
        int idx = mapping.idxByTag(tag);
        values.get(idx).copyFrom(null);
        selected.remove(idx);
    }

    public void deactivateAll() {
        if (selected.isEmpty()) {
            return;
        }
        TIntIterator iterator = selected.iterator();
        while (iterator.hasNext()) {
            int tag = iterator.next();
            iterator.remove();
            int idx = mapping.idxByTag(tag);
            values.get(idx).copyFrom(null);
        }
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        int n = buffer.getInt();
        selected.clear();
        for (int i = 0; i < n; i++) {
            int tag = buffer.getInt();
            int idx = mapping.idxByTag(tag);
            selected.add(idx);
            ((Serializable)values.get(idx)).deserialize(buffer);
        }
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(selected.size());
        if (selected.isEmpty()) {
            return;
        }
        TIntIterator iterator = selected.iterator();
        while (iterator.hasNext()) {
            int idx = iterator.next();
            int tag = mapping.tagByIdx(idx);
            buffer.putInt(tag);
            ((Serializable)values.get(idx)).serialize(buffer);
        }
    }

    public void iterateActive(Consumer<T> consumer) {
        selected.forEach(idx -> {
            consumer.accept(mapping.choiceTypeByIdx(idx));
            return true;
        });

    }

    public void iterateAll(Consumer<T> consumer) {
        for(int i = 0; i < mapping.nElements(); i++) {
            consumer.accept(mapping.choiceTypeByIdx(i));
        }
    }
}
