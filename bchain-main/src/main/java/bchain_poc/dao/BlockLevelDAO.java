package bchain_poc.dao;

import bchain_poc.domain.Hash;

public interface BlockLevelDAO {
    int getBlockLevel(Hash hash);

    void assignLevel(Hash hash, int level);
}
