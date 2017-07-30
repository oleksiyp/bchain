package bchain_poc.domain.impl;

import bchain_poc.domain.Hash;
import bchain_poc.domain.Tx;
import bchain_poc.domain.TxInput;
import bchain_poc.domain.TxOutput;
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
