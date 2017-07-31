package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

import java.util.List;
import java.util.Set;

public interface TxDao {
    List<Tx> all();

    List<Tx> allMatching(String criterion, Object... args);

    List<Tx> allWith(List<Hash> hashes);

    boolean hasTx(Hash hash);

    boolean hasAll(Set<Hash> hashes);

    void saveTx(Tx transaction);

    List<Tx> referencingTxs(Hash txHash);
}
