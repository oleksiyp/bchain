package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
@Getter
@ToString
@EqualsAndHashCode
public class PubKey {
    private final BigInteger modulus;
    private final BigInteger exponent;

    public static PubKey pubKey(byte[] modulus, byte[] exponent) {
        return new PubKey(new BigInteger(modulus), new BigInteger(exponent));
    }

    public void serialize(DataOutput dataOut) throws IOException {
        writeBigInt(dataOut, modulus);
        writeBigInt(dataOut, exponent);
    }

    private void writeBigInt(DataOutput dataOut, BigInteger val) throws IOException {
        byte[] exponent = val.toByteArray();
        dataOut.writeInt(exponent.length);
        dataOut.write(exponent);
    }

    public static PubKey deserialize(DataInput dataIn) throws IOException {
        return new PubKey(
                readBigInt(dataIn),
                readBigInt(dataIn));
    }

    private static BigInteger readBigInt(DataInput dataIn) throws IOException {
        int len = dataIn.readInt();
        byte[] data = new byte[len];
        dataIn.readFully(data);
        return new BigInteger(data);
    }

}
