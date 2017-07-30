package bchain_poc;

public interface Factory {
    <T> T create(Class<T> type);
}
