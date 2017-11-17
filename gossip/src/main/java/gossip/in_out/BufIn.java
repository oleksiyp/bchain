package gossip.in_out;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BufIn implements DataInput {
    private final ByteBuffer buf;

    public BufIn(ByteBuffer buf) {
        this.buf = buf;
    }



    @Override
    public boolean readBoolean() throws IOException {
        return buf.get() == 1;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return buf.get() & 0xFF;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int ch1 = buf.get() & 0xFF;
        int ch2 = buf.get() & 0xFF;
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public byte readByte() {
        return buf.get();
    }

    public void readFully(byte[] dst, int offset, int length) {
        buf.get(dst, offset, length);
    }

    public void readFully(byte[] dst) {
        buf.get(dst);
    }

    public char readChar() {
        return buf.getChar();
    }

    public short readShort() {
        return buf.getShort();
    }

    public int readInt() {
        return buf.getInt();
    }

    public long readLong() {
        return buf.getLong();
    }

    public float readFloat() {
        return buf.getFloat();
    }

    public double readDouble() {
        return buf.getDouble();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        buf.position(buf.position() + n);
        return n;
    }

    @Override
    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        throw new UnsupportedOperationException();
    }
}

