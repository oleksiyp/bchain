package bchain.dao;

import bchain.domain.Hash;

import java.util.Set;

public interface OrphanedBlockDao {
    boolean isOrphaned(Hash hash);

    void add(Hash hash);

    void remove(Hash hash);
}
