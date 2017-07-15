package node.datagram.shared;

import java.util.function.BiConsumer;

public interface Dispatcher<T> {
    void dispatch(int n, BiConsumer<Integer, T> consumer);
}
