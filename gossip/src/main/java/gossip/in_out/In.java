package gossip.in_out;

public interface In<T extends In> {
    byte get();

    T get(byte[] dst, int offset, int length);

    T get(byte[] dst);

    char getChar();

    short getShort();

    int getInt();

    long getLong();

    float getFloat();

    double getDouble();
}
