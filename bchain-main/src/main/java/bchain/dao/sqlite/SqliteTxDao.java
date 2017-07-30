package bchain.dao.sqlite;

import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;

public class SqliteTxDao implements TxDao {
    @Override
    public Tx findTx(Hash hash) {
        return null;
    }

    @Override
    public void saveTx(Tx transaction) {

    }
}
