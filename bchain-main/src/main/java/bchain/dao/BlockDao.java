package bchain.dao;

import bchain.domain.Block;
import bchain.domain.Hash;

public interface BlockDao {
    boolean hasBlock(Hash hash);

    void saveBlock(Block block);
}
