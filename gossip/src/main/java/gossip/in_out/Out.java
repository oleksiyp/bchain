package gossip.in_out;

import java.nio.ByteBuffer;

public interface Out<T extends Out> {
    T put(byte b);

    T put(ByteBuffer src);

    T put(byte[] src, int offset, int length);

    T put(byte[] src);

    T putChar(char value);

    T putShort(short value);

    T putInt(int value);

    T putLong(long value);

    T putFloat(float value);

    T putDouble(double value);
}
