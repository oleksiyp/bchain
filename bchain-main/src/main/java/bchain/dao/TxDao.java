package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

public interface TxDao {
    Tx findTx(Hash hash);

    void saveTx(Tx transaction);
}
