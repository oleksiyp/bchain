package bchain.domain;

public interface TxOutput {
    PubKey getAddress();

    long getValue();
}
