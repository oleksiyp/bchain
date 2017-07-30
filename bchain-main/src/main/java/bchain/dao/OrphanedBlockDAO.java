package bchain.dao;

import bchain.domain.Hash;

import java.util.Set;

public interface OrphanedBlockDAO {
    boolean isOrphanBlock(Hash hash);

    void addOrphanBlock(Hash from, Hash to);

    Set<Hash> resolvedOrphanBlock(Hash hash);

    void removeOrphanBlocks(Set<Hash> hashes);
}
