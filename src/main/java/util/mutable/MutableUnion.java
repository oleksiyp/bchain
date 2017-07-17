package util.mutable;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import io.netty.util.collection.IntObjectHashMap;
import util.Serializable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MutableUnion<T extends Choice<?>> implements Mutable<MutableUnion<T>>, Serializable {
    private int choice;
    private final Map<Integer, T> of;
    private final TIntIntMap mapping;
    private final int []reverseMapping;
    private final List<Mutable<?>> values;

    public <F> MutableUnion(Map<Integer, T> of, Object arg) {
        mapping = new TIntIntHashMap(of.size());
        reverseMapping = new int[of.size()];
        this.values = new ArrayList<>(of.size());

        this.of = new IntObjectHashMap<>(of.size());
        this.of.putAll(of);

        int i = 0;
        for (int tag : of.keySet()) {
            T choice = of.get(tag);
            mapping.put(tag, i);
            reverseMapping[i] = tag;
            values.add(choice.getConstructor().apply(arg));
            i++;
        }

        choice = -1;
    }

    @Override
    public void copyFrom(MutableUnion<T> obj) {
        if (choice != -1) {
            values.get(choice).clear();
        }
        choice = -1;

        if (obj == null) {
            return;
        }

        choice = obj.choice;
        if (choice != -1) {
            values.get(choice).copyFromObj(obj.values.get(choice));
        }
    }


    public <C extends Choice<R>, R extends Mutable<R>> R activate(C choice) {
        clear();
        int tag = choice.getTag();
        if (!mapping.containsKey(tag)) {
            throw new RuntimeException("no choice registered: " +
                    choice.getTag() + "=" + choice.getName());
        }
        this.choice = mapping.get(tag);
        Mutable<?> value = values.get(this.choice);
        return (R) value;
    }

    public T activeChoice() {
        if (choice == -1) {
            return null;
        }
        return of.get(reverseMapping[choice]);
    }

    public boolean isActive(T choice) {
        if (this.choice == -1) {
            return false;
        }
        return reverseMapping[this.choice] == choice.getTag();
    }

    public <C extends Choice<R>, R extends Mutable<R>> R get(C choice) {
        int tag = choice.getTag();
        if (!mapping.containsKey(tag)) {
            throw new RuntimeException("no choice registered: " +
                    tag + "=" + choice.getName());
        }
        int idx = mapping.get(tag);
        Mutable<?> value = values.get(idx);
        return (R) value;
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        if (buffer.get() == 1) {
            int tag = buffer.getInt();
            if (!mapping.containsKey(tag)) {
                throw new RuntimeException("no choice registered: " + tag);
            }
            choice = mapping.get(tag);
            ((Serializable)values.get(choice)).deserialize(buffer);
        } else {
            choice = -1;
        }

    }
    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put((byte)(choice != -1 ? 1 : 0));
        if (choice != -1) {
            buffer.putInt(reverseMapping[choice]);
            ((Serializable)values.get(choice)).serialize(buffer);
        }
    }

    @Override
    public String toString() {
        return choice != -1 ? values.get(choice).toString() : "NOT_ACTIVE";
    }
}
