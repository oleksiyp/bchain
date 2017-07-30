package bchain.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static bchain.domain.Tx.tx;

public class TxBuilder {
    @Getter
    private boolean coinbase;

    private final List<TxInput> inputs;

    private final List<TxOutput> outputs;

    public TxBuilder() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public Tx build() {
        Hash hash = TxHash.computeHash(coinbase, inputs, outputs);

        return tx(hash, coinbase, inputs, outputs);
    }

    public TxBuilder setCoinbase(boolean coinbase) {
        this.coinbase = coinbase;
        return this;
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
