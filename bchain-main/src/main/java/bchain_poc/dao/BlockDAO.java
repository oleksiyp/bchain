package bchain_poc.dao;

import bchain_poc.domain.Block;
import bchain_poc.domain.Hash;

public interface BlockDAO {
    boolean hasBlock(Hash hash);

    Block getBlock(Hash hash);

    void addBlock(Block block);

    Hash parentBlockHash(Hash hash);
}
