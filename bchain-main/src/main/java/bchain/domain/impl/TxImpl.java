package bchain.domain.impl;

import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import bchain.domain.TxOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class TxImpl implements Tx {
    Hash hash;
    List<TxInput> inputs;
    List<TxOutput> outputs;

    @Override
    public boolean verify() {
        return true;
    }
}
