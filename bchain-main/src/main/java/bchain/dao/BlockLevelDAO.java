package bchain.dao;

import bchain.domain.Hash;

public interface BlockLevelDAO {
    int getBlockLevel(Hash hash);

    void assignLevel(Hash hash, int level);
}
