package bchain.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static bchain.domain.Tx.tx;

public class TxBuilder {
    @Getter@Setter
    private boolean coinbase;

    private final List<TxInput> inputs;

    private final List<TxOutput> outputs;

    public TxBuilder() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public Tx build() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Hash hash;
        try (DataOutputStream dataOut = new DataOutputStream(out)) {
            digest(dataOut);
            hash = Hash.hashOf(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tx(hash, coinbase, inputs, outputs);
    }

    private void digest(DataOutputStream dataOut) throws IOException {
        for (TxInput input : inputs) {
            input.digest(dataOut);
        }

        for (TxOutput output : outputs) {
            output.digest(dataOut);
        }
    }

    public TxBuilder add(TxInput input) {
        inputs.add(input);
        return this;
    }

    public TxBuilder add(TxOutput output) {
        outputs.add(output);
        return this;
    }
}
