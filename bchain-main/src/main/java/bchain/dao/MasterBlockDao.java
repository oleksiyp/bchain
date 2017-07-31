package bchain.dao;

import bchain.domain.Hash;

public interface MasterBlockDao {
    Hash getMaster();

    void setMaster(Hash hash);
}
