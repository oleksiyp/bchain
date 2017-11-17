package gossip.in_out;

import java.io.IOException;
import java.nio.*;

public class BufOut implements java.io.DataOutput {
    private final ByteBuffer buf;

    public BufOut(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public void write(int b) {
        buf.put((byte) b);
    }

    @Override
    public void write(byte[] src, int offset, int length) {
        buf.put(src, offset, length);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        buf.put((byte)(v ? 1 : 0));
    }

    @Override
    public void writeByte(int v) throws IOException {
        buf.put((byte) v);
    }

    @Override
    public void write(byte[] src) {
        buf.put(src);
    }

    @Override
    public void writeChar(int value) {
        buf.putChar((char) value);
    }

    @Override
    public void writeShort(int value) {
        buf.putShort((short) value);
    }

    @Override
    public void writeInt(int value) {
        buf.putInt(value);
    }

    @Override
    public void writeLong(long value) {
        buf.putLong(value);
    }

    @Override
    public void writeFloat(float value) {
        buf.putFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        buf.putDouble(value);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            write((byte)s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        throw new UnsupportedOperationException();
    }
}

