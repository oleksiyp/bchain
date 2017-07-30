package bchain.domain;

public interface TxInput {
    Hash getOutputTxHash();

    int getOutputN();
}
