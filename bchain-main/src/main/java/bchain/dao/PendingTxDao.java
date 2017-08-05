package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

import java.util.List;

public interface PendingTxDao {
    void markPending(List<Hash> txs);

    void unmarkPending(List<Hash> txs);

    List<Tx> allTx();
}
