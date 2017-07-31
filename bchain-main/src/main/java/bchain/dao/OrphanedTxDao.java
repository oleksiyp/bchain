package bchain.dao;

import bchain.domain.Hash;

import java.util.Set;

public interface OrphanedTxDao {
    Set<Hash> all();

    boolean isOrphaned(Hash hash);

    boolean isOrphanedAny(Set<Hash> txHashes);

    void add(Hash hash);

    void remove(Hash hash);

}
