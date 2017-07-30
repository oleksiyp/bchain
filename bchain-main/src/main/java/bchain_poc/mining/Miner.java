package bchain_poc.mining;

import bchain_poc.domain.Hash;

public interface Miner {
    void pendingTx(Hash tx, boolean added);

    void masterSwitched(Hash master);
}
