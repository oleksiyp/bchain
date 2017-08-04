package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.DataInput;
import java.io.DataOutput;
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

    public void digest(DataOutput dataOut) throws IOException {
        serialize(dataOut);
    }

    public void serialize(DataOutput dataOut) throws IOException {
        address.serialize(dataOut);
        dataOut.writeLong(value);
    }


    public static TxOutput deserialize(DataInput dataIn) throws IOException {
        return new TxOutput(
                PubKey.deserialize(dataIn),
                dataIn.readLong());
    }
}
