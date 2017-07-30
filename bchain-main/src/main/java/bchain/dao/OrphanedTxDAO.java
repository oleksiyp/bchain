package bchain.dao;

import bchain.domain.Hash;
import com.google.common.collect.Multimap;

import java.util.Set;

public interface OrphanedTxDAO {
    boolean isOrphanTx(Hash txHash);

    void addAll(Multimap<Hash, Hash> hash);

    Set<Hash> resolvedOrphanTx(Hash hash);

    void removeOrphanTxs(Set<Hash> deOrphaned);
}
