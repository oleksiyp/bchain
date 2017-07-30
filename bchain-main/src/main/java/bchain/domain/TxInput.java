package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
        return new TxInput(prevTxHash, outputIndex, signature);
    }

    public void digest(DataOutputStream out) throws IOException {
        prevTxHash.digest(out);
        out.writeInt(outputIndex);
        out.writeInt(signature.length);
        out.write(signature);
    }
}
