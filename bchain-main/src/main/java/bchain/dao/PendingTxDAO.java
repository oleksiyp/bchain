package bchain.dao;

import bchain.domain.Hash;

public interface PendingTxDAO {
    void markPending(Hash txHash, boolean value);
}
