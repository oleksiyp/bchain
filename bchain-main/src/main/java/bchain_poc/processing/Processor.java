package bchain_poc.processing;

import bchain_poc.domain.Block;
import bchain_poc.domain.Tx;

public class Processor {
    private Processor next;

    public Processor(Processor next) {
        this.next = next;
    }

    public void addBlock(Block block) {
        if (next != null) {
            next.addBlock(block);
        }
    }

    public void addTransaction(Tx tx) {
        if (next != null) {
            next.addTransaction(tx);
        }
    }
}
