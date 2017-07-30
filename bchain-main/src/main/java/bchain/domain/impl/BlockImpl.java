package bchain.domain.impl;

import bchain.domain.Block;
import bchain.domain.Hash;
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
