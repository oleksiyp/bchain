package bchain.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hash {
    private final byte []hashValue;

    public byte[] getValues() {
        return Arrays.copyOf(hashValue, hashValue.length);
    }

    public static final Hash hash(byte []hashValue) {
        return new Hash(hashValue);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(hashValue.length * 2);
        for (int i = 0; i < hashValue.length && i < 4; i++) {
            byte b = hashValue[i];
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static Hash hashOf(String str) {
        return hashOf(str.getBytes());
    }

    public static Hash hashOf(byte[] bytes) {
        return hash(Crypto.hash(bytes));
    }

    public void serialize(DataOutput dataOut) throws IOException {
        dataOut.writeInt(hashValue.length);
        dataOut.write(hashValue);
    }

    public static void serializeNullable(Hash hash, DataOutput dataOut) throws IOException {
        if (hash != null) {
            dataOut.writeBoolean(true);
            hash.serialize(dataOut);
        } else {
            dataOut.writeBoolean(false);
        }
    }

    public static Hash deserialize(DataInput dataIn) throws IOException {
        int len = dataIn.readInt();
        byte []hash = new byte[len];
        dataIn.readFully(hash);
        return hash(hash);
    }
}
