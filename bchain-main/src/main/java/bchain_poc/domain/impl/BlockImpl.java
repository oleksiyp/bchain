package bchain_poc.domain.impl;

import bchain_poc.domain.Block;
import bchain_poc.domain.Hash;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlockImpl implements Block {
    private boolean genesis;
    private Hash hash;
    private Hash prevBlockHash;
    private List<Hash> txs;

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
