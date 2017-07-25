package node2.registry;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Pool<C> implements Consumer<C> {
    private final ArrayList<C> values;
    private int size;

    public Pool(int size) {
        values = new ArrayList<>(size);
        this.size = size;
    }

    public C get() {
        if (values.isEmpty()) {
            return null;
        }
        return values.remove(values.size() - 1);
    }

    @Override
    public void accept(C c) {
        if (values.size() < size) {
            values.add(c);
        }
    }
}
