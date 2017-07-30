package bchain.domain.impl;

import bchain.domain.Hash;
import bchain.domain.TxInput;

public class TxInputImpl implements TxInput {
    @Override
    public Hash getOutputTxHash() {
        return null;
    }

    @Override
    public int getOutputN() {
        return 0;
    }
}
