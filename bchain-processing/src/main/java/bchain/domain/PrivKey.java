package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigInteger;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PrivKey extends RsaKey {
    protected PrivKey(BigInteger modulus, BigInteger exponent) {
        super(modulus, exponent);
    }

    public static PrivKey privKey(byte[] modulus, byte[] exponent) {
        return new PrivKey(new BigInteger(modulus), new BigInteger(exponent));
    }

    public static PrivKey deserialize(DataInput dataIn) throws IOException {
        return new PrivKey(
                readBigInt(dataIn),
                readBigInt(dataIn));
    }
}
