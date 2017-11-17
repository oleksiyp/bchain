package gossip.in_out;

import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CountOut implements java.io.DataOutput {
    @Getter
    private int count;

    public void reset() {
        count = 0;
    }

    @Override
    public void write(int b) {
        count++;
    }

    @Override
    public void write(byte[] src, int offset, int length) {
        count += length;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        count++;
    }

    @Override
    public void writeByte(int v) throws IOException {
        count++;
    }

    @Override
    public void write(byte[] src) {
        count += src.length;
    }

    @Override
    public void writeChar(int value) {
        count += 2;
    }

    @Override
    public void writeShort(int value) {
        count += 2;
    }

    @Override
    public void writeInt(int value) {
        count += 4;
    }

    @Override
    public void writeLong(long value) {
        count += 8;
    }

    @Override
    public void writeFloat(float value) {
        count += 4;
    }

    @Override
    public void writeDouble(double value) {
        count += 8;
    }

    @Override
    public void writeBytes(String s) throws IOException {
        count += s.length();
    }

    @Override
    public void writeChars(String s) throws IOException {
        count += s.length() * 2;
    }

    @Override
    public void writeUTF(String s) throws IOException {
        throw new UnsupportedOperationException();
    }
}

