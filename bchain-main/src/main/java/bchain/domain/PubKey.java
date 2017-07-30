package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
}
