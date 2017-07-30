package bchain.processing.receive;

import bchain.Factory;
import bchain.domain.Block;
import bchain.processing.Processor;
import bchain.domain.Hash;
import bchain.dao.BlockDAO;

public class ReceiveBlockProcessor extends Processor {
    private static final Hash GENESIS_HASH = new Hash("gen_hash");

    private final BlockDAO blockDAO;

    public ReceiveBlockProcessor(Factory factory, Processor next) {
        super(next);
        blockDAO = factory.create(BlockDAO.class);
    }

    public void addBlock(Block block) {
        if (!block.verify()) {
            throw new RuntimeException("Block verification failed");
        }

        if (blockDAO.hasBlock(block.getHash())) {
            return;
        }

        if (block.isGenesis() && !GENESIS_HASH.equals(block.getHash())) {
            throw new RuntimeException("Bad genesis block hash");
        }

        blockDAO.addBlock(block);

        super.addBlock(block);
    }
}
