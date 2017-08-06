package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.*;
import java.math.BigInteger;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor(access = PROTECTED)
@Getter
@EqualsAndHashCode
public abstract class RsaKey {
    private final BigInteger modulus;
    private final BigInteger exponent;

    public void serialize(DataOutput dataOut) throws IOException {
        writeBigInt(dataOut, modulus);
        writeBigInt(dataOut, exponent);
    }

    private void writeBigInt(DataOutput dataOut, BigInteger val) throws IOException {
        byte[] exponent = val.toByteArray();
        dataOut.writeInt(exponent.length);
        dataOut.write(exponent);
    }

    protected static BigInteger readBigInt(DataInput dataIn) throws IOException {
        int len = dataIn.readInt();
        byte[] data = new byte[len];
        dataIn.readFully(data);
        return new BigInteger(data);
    }

    public static RsaKeyPair gen() {
        return Crypto.rsaGen();
    }

    @Override
    public String toString() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            serialize(dataOut);

            return getClass().getSimpleName() + "(" +
                    Hash.hashOf(byteOut.toByteArray()) +
            ')';
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
