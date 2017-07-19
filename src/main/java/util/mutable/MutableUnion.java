package util.mutable;

import node.factory.RegistryMapping;
import util.Serializable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MutableUnion<T extends ChoiceType<?>> implements Mutable<MutableUnion<T>>, Serializable {
    private int choice;
    private final List<Mutable<?>> values;
    private RegistryMapping<T> mapping;

    public <F> MutableUnion(RegistryMapping<T> mapping) {
        this.mapping = mapping;
        choice = -1;
        values = new ArrayList<>(mapping.nElements());
        for (int i = 0; i < mapping.nElements(); i++) {
            values.add(mapping.getConstructor(i).get());
        }
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


    public <C extends ChoiceType<R>, R extends Mutable<R>> R activate(C choice) {
        clear();
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            throw new RuntimeException(choice + " not registered in " + mapping);
        }
        this.choice = mapping.idxByTag(tag);
        Mutable<?> value = values.get(this.choice);
        return (R) value;
    }

    public T activeChoice() {
        if (choice == -1) {
            return null;
        }
        return mapping.choiceTypeByIdx(choice);
    }

    public boolean isActive(T choice) {
        if (this.choice == -1) {
            return false;
        }
        return mapping.choiceTypeByIdx(this.choice) == choice;
    }

    public <C extends ChoiceType<R>, R extends Mutable<R>> R get(C choice) {
        int tag = mapping.tagByChoiceType(choice);
        if (tag == -1) {
            throw new RuntimeException(choice + " not registered in " + mapping);
        }
        int idx = mapping.idxByTag(tag);
        Mutable<?> value = values.get(idx);
        return (R) value;
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        if (buffer.get() == 1) {
            int tag = buffer.getInt();
            int idx = mapping.idxByTag(tag);
            if (idx == -1) {
                throw new RuntimeException(tag + " not registered in " + mapping);
            }
            choice = idx;
            ((Serializable)values.get(choice)).deserialize(buffer);
        } else {
            choice = -1;
        }

    }
    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put((byte)(choice != -1 ? 1 : 0));
        if (choice != -1) {
            buffer.putInt(mapping.tagByIdx(choice));
            ((Serializable)values.get(choice)).serialize(buffer);
        }
    }

    @Override
    public String toString() {
        return choice != -1 ? values.get(choice).toString() : "NOT_ACTIVE";
    }
}
