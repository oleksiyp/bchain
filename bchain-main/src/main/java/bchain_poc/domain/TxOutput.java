package bchain_poc.domain;

public interface TxOutput {
    PubKey getAddress();

    long getValue();
}
