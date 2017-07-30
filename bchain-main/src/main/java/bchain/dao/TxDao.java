package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

public interface TxDao {
    boolean hasTx(Hash hash);

    void saveTx(Tx transaction);
}
