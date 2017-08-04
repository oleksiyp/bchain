package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static bchain.domain.TxHash.computeHash;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;

@ToString
@EqualsAndHashCode
public class Tx {
    @Getter
    private final Hash hash;

    @Getter
    private final boolean coinbase;

    @Getter
    private final List<TxInput> inputs;

    @Getter
    private final List<TxOutput> outputs;

    private Tx(Hash hash,
              boolean coinbase,
              List<TxInput> inputs,
              List<TxOutput> outputs) {
        this.hash = hash;
        this.coinbase = coinbase;
        this.inputs = copyOf(inputs);
        this.outputs = copyOf(outputs);
    }

    public boolean verify() {
        Hash computedHash = computeHash(coinbase, inputs, outputs);

        return computedHash.equals(this.hash);
    }

    public static TxBuilder builder() {
        return new TxBuilder();
    }

    public static Tx tx(Hash hash,
                        boolean coinbase,
                        List<TxInput> inputs,
                        List<TxOutput> outputs) {
        return new Tx(hash, coinbase, inputs, outputs);
    }

    public void digest(DataOutputStream dataOut) throws IOException {
        computeHash(coinbase, inputs, outputs)
                .digest(dataOut);
    }

    public void serialize(DataOutput dataOut) throws IOException {
        hash.serialize(dataOut);
        dataOut.writeBoolean(coinbase);
        dataOut.writeInt(inputs.size());
        for (TxInput input : inputs) {
            input.serialize(dataOut);
        }
        dataOut.writeInt(outputs.size());
        for (TxOutput output : outputs) {
            output.serialize(dataOut);
        }
    }

    public static Tx deserialize(DataInput dataIn) throws IOException {
        Hash hash = Hash.deserialize(dataIn);
        boolean coinbase = dataIn.readBoolean();
        int nInputs = dataIn.readInt();
        List<TxInput> inputs = new ArrayList<>(nInputs);
        for (int i = 0; i < nInputs; i++) {
            inputs.add(TxInput.deserialize(dataIn));
        }

        int nOutputs = dataIn.readInt();
        List<TxOutput> outputs = new ArrayList<>(nOutputs);
        for (int i = 0; i < nOutputs; i++) {
            outputs.add(TxOutput.deserialize(dataIn));
        }
        return new Tx(hash, coinbase, inputs, outputs);
    }
}
