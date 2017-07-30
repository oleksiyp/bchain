package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
@ToString
@EqualsAndHashCode
public class TxOutput {
    private final PubKey address;
    private final long value;

    public static TxOutput output(PubKey address, long value) {
        return new TxOutput(address, value);
    }

    public void digest(DataOutputStream dataOut) throws IOException {
        writeAddress(dataOut);
        dataOut.writeLong(value);
    }

    private void writeAddress(DataOutputStream dataOut) throws IOException {
        writeBigInt(dataOut, this.address.getModulus());
        writeBigInt(dataOut, this.address.getExponent());
    }

    private void writeBigInt(DataOutputStream dataOut, BigInteger val) throws IOException {
        byte[] exponent = val.toByteArray();
        dataOut.writeInt(exponent.length);
        dataOut.write(exponent);
    }
}
