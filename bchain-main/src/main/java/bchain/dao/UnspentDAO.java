package bchain.dao;

import bchain.domain.PubKey;

public interface UnspentDAO {
    void unspentPrepareAddRemove(PubKey address, boolean add, long value);

    void unspentCommitAddRemove(PubKey address, boolean add, long value, boolean rollback);
}
