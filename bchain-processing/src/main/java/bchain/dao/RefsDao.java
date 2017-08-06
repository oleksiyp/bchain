package bchain.dao;

import bchain.domain.Hash;

public interface RefsDao {
    Hash getHead();

    void setHead(Hash head);

    Hash getMaster();

    void setMaster(Hash hash);
}
