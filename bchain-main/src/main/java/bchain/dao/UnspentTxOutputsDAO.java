package bchain.dao;

import bchain.domain.Hash;

public interface UnspentTxOutputsDAO {
    boolean unspentOutAddRemove(boolean add, Hash outTxHash, int n);
}
