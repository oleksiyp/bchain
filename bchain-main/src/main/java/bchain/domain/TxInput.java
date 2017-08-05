package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.copyOf;

@ToString
@EqualsAndHashCode
public class TxInput {
    @Getter
    private final Hash prevTxHash;

    @Getter
    private final int outputIndex;

    private final byte[] signature;

    public TxInput(Hash prevTxHash,
                   int outputIndex,
                   byte[] signature) {
        this.prevTxHash = prevTxHash;
        this.outputIndex = outputIndex;
        this.signature = copyOf(signature, signature.length);
    }

    public byte[] getSignature() {
        return copyOf(signature, signature.length);
    }

    public static TxInput input(Hash prevTxHash, int outputIndex, byte[] signature) {
        return new TxInput(
                checkNotNull(prevTxHash),
                outputIndex,
                checkNotNull(signature));
    }

    public void digest(DataOutput out) throws IOException {
        serialize(out);
    }

    public void serialize(DataOutput dataOut) throws IOException {
        prevTxHash.serialize(dataOut);
        dataOut.writeInt(outputIndex);
        dataOut.writeInt(signature.length);
        dataOut.write(signature);
    }

    public static TxInput deserialize(DataInput dataIn) throws IOException {
        Hash prevTxHash = Hash.deserialize(dataIn);
        int outputIndex = dataIn.readInt();
        int len = dataIn.readInt();
        byte []signature = new byte[len];
        dataIn.readFully(signature);
        return new TxInput(prevTxHash, outputIndex, signature);
    }
}
