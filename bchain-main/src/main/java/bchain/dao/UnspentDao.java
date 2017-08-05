package bchain.dao;

import bchain.domain.Hash;
import bchain.domain.PubKey;
import bchain.domain.UnspentTxOut;

import java.util.List;
import java.util.Set;

public interface UnspentDao {
    long unspentAmount(PubKey address);

    void spendUnspend(List<UnspentTxOut> unspentTxOuts, List<UnspentTxOut> removeUnspentTxOuts);

    UnspentTxOut get(Hash hash, int outputIndex);
}
