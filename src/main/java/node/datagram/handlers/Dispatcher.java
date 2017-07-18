package node.datagram.handlers;

import java.util.function.BiConsumer;

public interface Dispatcher<T> {
    void dispatch(int n, BiConsumer<Integer, T> consumer);
}
