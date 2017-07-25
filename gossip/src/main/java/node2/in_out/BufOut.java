package node2.in_out;

import java.nio.*;

public class BufOut implements Out<BufOut> {
    private final ByteBuffer buf;

    public BufOut(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public BufOut put(byte b) {
        buf.put(b);
        return this;
    }

    @Override
    public BufOut put(ByteBuffer src) {
        buf.put(src);
        return this;
    }

    @Override
    public BufOut put(byte[] src, int offset, int length) {
        buf.put(src, offset, length);
        return this;
    }

    @Override
    public BufOut put(byte[] src) {
        buf.put(src);
        return this;
    }

    @Override
    public BufOut putChar(char value) {
        buf.putChar(value);
        return this;
    }

    @Override
    public BufOut putShort(short value) {
        buf.putShort(value);
        return this;
    }

    @Override
    public BufOut putInt(int value) {
        buf.putInt(value);
        return this;
    }

    @Override
    public BufOut putLong(long value) {
        buf.putLong(value);
        return this;
    }

    @Override
    public BufOut putFloat(float value) {
        buf.putFloat(value);
        return this;
    }

    @Override
    public BufOut putDouble(double value) {
        buf.putDouble(value);
        return this;
    }
}

