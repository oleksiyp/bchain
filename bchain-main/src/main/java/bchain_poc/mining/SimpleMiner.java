package bchain_poc.mining;

import bchain_poc.Factory;
import bchain_poc.domain.Hash;

public class SimpleMiner implements Miner {
    public SimpleMiner(Factory factory) {
    }

    @Override
    public void pendingTx(Hash tx, boolean added) {
        if (added) {
            System.out.println("Pending tx: " + tx);
        } else {
            System.out.println("Remove pending tx: " + tx);
        }
    }

    @Override
    public void masterSwitched(Hash master) {
        System.out.println("New master: " + master);
    }
}
