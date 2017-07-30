package bchain_poc.dao;

import bchain_poc.domain.PubKey;

public interface UnspentDAO {
    void unspentPrepareAddRemove(PubKey address, boolean add, long value);

    void unspentCommitAddRemove(PubKey address, boolean add, long value, boolean rollback);
}
