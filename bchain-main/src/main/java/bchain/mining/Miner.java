package bchain.mining;

import bchain.domain.Hash;

public interface Miner {
    void pendingTx(Hash tx, boolean added);

    void masterSwitched(Hash master);
}
