package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.PubKey;

public interface UnspentDao {
    Hash getHead();

    void setHead(Hash head);

    void addTxOut(Hash txHash, int index);

    void removeTxOut(Hash hash, int index);

    void changeUnspent(PubKey address, long value);
}
