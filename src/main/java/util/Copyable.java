package util;

public interface Copyable<T extends Copyable<T>> {
    void copyTo(T obj);
}
