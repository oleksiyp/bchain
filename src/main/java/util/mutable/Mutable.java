package util.mutable;

public interface Mutable<T extends Mutable<T>> {
    void copyFrom(T obj);

    default void copyFrom(Object obj) {
        copyFrom((T) obj);
    }
}
