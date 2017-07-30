package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

import java.util.List;

public interface TxDAO {
    boolean hasTx(Hash hash);

    void addTx(Tx tx);

    Tx getTx(Hash hash);

    List<Tx> getAllTx(List<Hash> txs);
}
