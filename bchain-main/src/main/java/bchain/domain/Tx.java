package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import static bchain.domain.TxHash.computeHash;
import static com.google.common.collect.ImmutableList.copyOf;

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
}
