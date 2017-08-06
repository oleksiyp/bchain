package bchain.domain;

import lombok.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.copyOf;

@EqualsAndHashCode
public class TxInput {
    @Getter
    private final Hash prevTxHash;

    @Getter
    private final int outputIndex;

    private final byte[] signature;

    private TxInput(Hash prevTxHash,
                   int outputIndex,
                   byte[] signature) {
        this.prevTxHash = prevTxHash;
        this.outputIndex = outputIndex;
        this.signature = copyOf(signature, signature.length);
    }

    public byte[] getSignature() {
        return copyOf(signature, signature.length);
    }

    public static TxInput input(Hash prevTxHash, int outputIndex) {
        return new TxInput(
                checkNotNull(prevTxHash),
                outputIndex,
                new byte[0]);
    }

    public static TxInput input(Hash prevTxHash, int outputIndex, byte[] signature) {
        return new TxInput(
                checkNotNull(prevTxHash),
                outputIndex,
                checkNotNull(signature));
    }

    public TxInput sign(PrivKey privKey, boolean coinbase, List<TxOutput> outputs) {
        byte[] bytes = Crypto.inputDigest(coinbase, prevTxHash, outputIndex, outputs);

        byte[] sign = Crypto.sign(privKey, bytes);

        return new TxInput(prevTxHash, outputIndex, sign);
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

    @Override
    public String toString() {
        return "TxInput(" +
                "prevTxHash=" + prevTxHash +
                ", outputIndex=" + outputIndex +
                ", signature=" + Hash.hash(signature) +
                ')';
    }
}
