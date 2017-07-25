package gossip.in_out;

import java.nio.ByteBuffer;

public class BufIn implements In<BufIn> {
    private final ByteBuffer buf;

    public BufIn(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public byte get() {
        return buf.get();
    }

    @Override
    public BufIn get(byte[] dst, int offset, int length) {
        buf.get(dst, offset, length);
        return this;
    }

    @Override
    public BufIn get(byte[] dst) {
        buf.get(dst);
        return this;
    }

    @Override
    public char getChar() {
        return buf.getChar();
    }

    @Override
    public short getShort() {
        return buf.getShort();
    }

    @Override
    public int getInt() {
        return buf.getInt();
    }

    @Override
    public long getLong() {
        return buf.getLong();
    }

    @Override
    public float getFloat() {
        return buf.getFloat();
    }

    @Override
    public double getDouble() {
        return buf.getDouble();
    }

}

