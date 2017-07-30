package bchain_poc.domain;

import java.util.List;

public interface Tx {
    Hash getHash();

    List<TxInput> getInputs();

    List<TxOutput> getOutputs();

    boolean verify();
}
