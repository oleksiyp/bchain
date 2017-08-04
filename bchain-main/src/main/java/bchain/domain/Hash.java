package bchain.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hash {
    public static final Hash ALL = hash("*".getBytes());
    private final byte []hashValue;

    public byte[] getValues() {
        return Arrays.copyOf(hashValue, hashValue.length);
    }

    public static final Hash hash(byte []hashValue) {
        return new Hash(hashValue);
    }

    public void digest(DataOutputStream out) throws IOException {
        out.writeInt(hashValue.length);
        out.write(hashValue);
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(bytes);
            return hash(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void digest(Hash hash, DataOutputStream dataOut) throws IOException {
        if (hash != null) {
            hash.digest(dataOut);
        } else {
            dataOut.write(0);
        }
    }

    public void serialize(DataOutput dataOut) throws IOException {
        dataOut.writeInt(hashValue.length);
        dataOut.write(hashValue);
    }

    public static Hash deserialize(DataInput dataIn) throws IOException {
        int len = dataIn.readInt();
        byte []hash = new byte[len];
        dataIn.readFully(hash);
        return hash(hash);
    }
}
