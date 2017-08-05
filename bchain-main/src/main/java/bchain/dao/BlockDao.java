package bchain.dao;

import bchain.domain.Block;
import bchain.domain.Hash;

import java.util.List;

public interface BlockDao {
    boolean hasBlock(Hash hash);

    void saveBlock(Block block);

    List<Block> all();

    List<Block> allWith(List<Hash> hashes);

    Block get(Hash from);

    List<Block> referencingBlocksByTx(Hash txHash);

    List<Block> referencingBlocksByBlock(Hash blockHash);
}
