package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

import java.util.List;
import java.util.Set;

public interface TxDao {
    List<Tx> all();

    List<Tx> allWith(List<Hash> hashes);

    boolean hasTx(Hash hash);

    boolean hasAll(Set<Hash> hashes);

    boolean saveTx(Tx transaction);

    List<Tx> referencingTxs(Hash txHash);
}
