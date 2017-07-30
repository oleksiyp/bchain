package bchain.dao;

import bchain.domain.Block;
import bchain.domain.Hash;

public interface BlockDAO {
    boolean hasBlock(Hash hash);

    Block getBlock(Hash hash);

    void addBlock(Block block);

    Hash parentBlockHash(Hash hash);
}
