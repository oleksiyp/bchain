package bchain_poc.dao;

import bchain_poc.domain.Hash;

public interface PendingTxDAO {
    void markPending(Hash txHash, boolean value);
}
