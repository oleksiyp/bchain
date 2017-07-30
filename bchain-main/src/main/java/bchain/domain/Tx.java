package bchain.domain;

import lombok.Getter;

public class Tx {
    @Getter private Hash hash;

    public boolean verify() {
        return false;
    }

    public static TxBuilder builder() {
        return new TxBuilder();
    }
}
