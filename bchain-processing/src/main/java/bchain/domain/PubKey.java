package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
public class PubKey extends RsaKey {
    protected PubKey(BigInteger modulus, BigInteger exponent) {
        super(modulus, exponent);
    }

    public static PubKey pubKey(byte[] modulus, byte[] exponent) {
        return new PubKey(new BigInteger(modulus), new BigInteger(exponent));
    }

    public static PubKey deserialize(DataInput dataIn) throws IOException {
        return new PubKey(
                readBigInt(dataIn),
                readBigInt(dataIn));
    }
}
