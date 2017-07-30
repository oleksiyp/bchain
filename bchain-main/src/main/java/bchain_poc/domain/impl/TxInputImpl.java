package bchain_poc.domain.impl;

import bchain_poc.domain.Hash;
import bchain_poc.domain.TxInput;

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
