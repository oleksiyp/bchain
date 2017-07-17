package util.mutable;

import java.util.function.Function;

public interface Choice<T extends Mutable<T>> {
    int getTag();

    String getName();

    Class<T> getType();

    Function<Object, T> getConstructor();
}
