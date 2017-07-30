package bchain;

public interface Factory {
    <T> T create(Class<T> type);
}
