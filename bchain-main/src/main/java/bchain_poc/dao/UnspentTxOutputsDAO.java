package bchain_poc.dao;

import bchain_poc.domain.Hash;

public interface UnspentTxOutputsDAO {
    boolean unspentOutAddRemove(boolean add, Hash outTxHash, int n);
}
