package bchain_poc.dao;

import bchain_poc.domain.Hash;

public interface ChainDAO {
    enum BlockType {
        INVALID
    }

    void declare(Hash hash, BlockType invalid);
}
