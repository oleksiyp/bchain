package util.mutable;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lombok.ToString;
import node.datagram.Choice;
import util.Serializable;

import java.nio.ByteBuffer;
import java.util.*;

@ToString(of = "values", includeFieldNames = false)
public class MutableSet<T extends Choice<?>> implements Mutable<MutableSet<T>>, Serializable {
    private TIntSet selected;
    private Map<Integer, T> of;
    private final Map<T, Mutable<?>> values;

    public MutableSet(Map<Integer, T> of) {
        this.of = of;
        selected = new TIntHashSet(of.size());
        values = new HashMap<>(of.size());
        of.forEach((tag, type) -> values.put(type, type.getConstructor().get()));
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
            int tag = iterator.next();
            T type = of.get(tag);
            values.get(type).copyFrom(obj.values.get(type));
            selected.add(tag);
        }
    }

    public <C extends Choice<R>, R extends Mutable<R> & Serializable> R activate(C type) {
        this.selected.add(type.getTag());
        return (R) values.get(type);
    }

    public boolean isActive(T type) {
        return selected.contains(type.getTag());
    }

    public <C extends Choice<R>, R extends Mutable<R> & Serializable> R get(C type) {
        return (R) values.get(type);
    }

    public void deactivate(T type) {
        values.get(type).copyFrom(null);
        selected.remove(type.getTag());
    }

    public void deactivateAll() {
        if (selected.isEmpty()) {
            return;
        }
        TIntIterator iterator = selected.iterator();
        while (iterator.hasNext()) {
            int tag = iterator.next();
            iterator.remove();
            T type = of.get(tag);
            values.get(type).copyFrom(null);
        }
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        int n = buffer.getInt();
        selected.clear();
        for (int i = 0; i < n; i++) {
            int tag = buffer.getInt();
            T type = of.get(tag);
            selected.add(tag);
            ((Serializable)values.get(type)).deserialize(buffer);
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
            int tag = iterator.next();
            buffer.putInt(tag);
            T type = of.get(tag);
            ((Serializable)values.get(type)).serialize(buffer);
        }
    }
}
