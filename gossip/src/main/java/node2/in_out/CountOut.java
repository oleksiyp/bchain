package node2.in_out;

import java.nio.ByteBuffer;

public class CountOut implements Out<CountOut> {
    private int size;

    public CountOut() {
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public void reset() {
        size = 0;
    }

    @Override
    public CountOut put(byte b) {
        size += Byte.BYTES;
        return this;
    }

    @Override
    public CountOut put(ByteBuffer src) {
        size += src.remaining();
        return this;
    }

    @Override
    public CountOut put(byte[] src, int offset, int length) {
        size += length;
        return this;
    }

    @Override
    public CountOut put(byte[] src) {
        size += src.length;
        return this;
    }

    @Override
    public CountOut putChar(char value) {
        size += Character.BYTES;
        return this;
    }

    @Override
    public CountOut putShort(short value) {
        size += Short.BYTES;
        return this;
    }

    @Override
    public CountOut putInt(int value) {
        size += Integer.BYTES;
        return this;
    }

    @Override
    public CountOut putLong(long value) {
        size += Long.BYTES;
        return this;
    }

    @Override
    public CountOut putFloat(float value) {
        size += Float.BYTES;
        return this;
    }

    @Override
    public CountOut putDouble(double value) {
        size += Double.BYTES;
        return this;
    }
}

