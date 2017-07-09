package util;

public interface Pool {
    <T> T newInstance(Class<T> clazz);

    void returnToPool(Object o);
}
