package bchain.dao;

import bchain.domain.Hash;

public interface ChainDAO {
    enum BlockType {
        INVALID
    }

    void declare(Hash hash, BlockType invalid);
}
