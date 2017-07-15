package node.datagram;

import util.mutable.Mutable;

import java.util.function.Supplier;

public interface Choice<T extends Mutable<T>> {
    int getTag();

    String getName();

    Class<T> getType();

    Supplier<T> getConstructor();
}
