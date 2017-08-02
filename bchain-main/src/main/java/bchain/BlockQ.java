package bchain;

import bchain.app.BlockAcceptor;
import bchain.domain.Block;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockQ implements BlockAcceptor {
    Queue<Block> blocks;

    public BlockQ() {
        blocks = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean accept(Block block) {
        if (!block.getHash().toString().startsWith("0")) {
            return false;
        }

        blocks.add(block);
        return true;
    }
}
