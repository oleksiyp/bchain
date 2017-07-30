package bchain_poc.domain;

public interface TxInput {
    Hash getOutputTxHash();

    int getOutputN();
}
