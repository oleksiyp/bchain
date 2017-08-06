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
    private final List<PrivKey> privKeys;

    public TxBuilder() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        privKeys = new ArrayList<>();
    }

    public Tx build() {
        List<TxInput> signedInputs = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            TxInput input = inputs.get(i);
            PrivKey privKey = privKeys.get(i);
            TxInput signedInput = input.sign(privKey, coinbase, outputs);
            signedInputs.add(signedInput);
        }

        Hash hash = Crypto.computeTxHash(coinbase, signedInputs, outputs);

        return tx(hash, coinbase, signedInputs, outputs);
    }

    public TxBuilder setCoinbase(boolean coinbase) {
        this.coinbase = coinbase;
        return this;
    }

    public TxBuilder add(TxInput input, PrivKey privKey) {
        inputs.add(input);
        privKeys.add(privKey);
        return this;
    }

    public TxBuilder add(TxOutput output) {
        outputs.add(output);
        return this;
    }
}
