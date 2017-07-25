package util;

public interface Mutable<T extends Mutable<T>> {
    void copyFrom(T obj);

    default void copyFromObj(Object obj) {
        copyFrom((T) obj);
    }

    default void clear() {
        copyFrom(null);
    }
}
