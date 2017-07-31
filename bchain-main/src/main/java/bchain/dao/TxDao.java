package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.Tx;

import java.util.List;

public interface TxDao {
    List<Tx> all();

    List<Tx> allMatching(String criterion, Object... args);

    boolean hasTx(Hash hash);

    void saveTx(Tx transaction);

    List<Tx> allWith(List<Hash> hashes);
}
