package bchain.dao;

import bchain.domain.Hash;

public interface BlockLevelDao {
    int getLevel(Hash hash);

    void setLevel(Hash hash, int level);
}
