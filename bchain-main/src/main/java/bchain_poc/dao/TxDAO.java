package bchain_poc.dao;

import bchain_poc.domain.Hash;
import bchain_poc.domain.Tx;

import java.util.List;

public interface TxDAO {
    boolean hasTx(Hash hash);

    void addTx(Tx tx);

    Tx getTx(Hash hash);

    List<Tx> getAllTx(List<Hash> txs);
}
