package bchain_poc.processing.orphan;

import bchain_poc.Factory;
import bchain_poc.domain.Block;
import bchain_poc.processing.Processor;
import bchain_poc.domain.Hash;
import bchain_poc.dao.*;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class OrphanBlockProcessor extends Processor {
    BlockDAO blockDAO;

    OrphanedBlockDAO orphanedBlockDAO;

    BlockLevelDAO blockLevelDAO;

    public OrphanBlockProcessor(Factory factory, Processor next) {
        super(next);

        blockDAO = factory.create(BlockDAO.class);
        orphanedBlockDAO = factory.create(OrphanedBlockDAO.class);
        blockLevelDAO = factory.create(BlockLevelDAO.class);
    }

    @Override
    public void addBlock(Block block) {
        if (block.isGenesis()) {
            super.addBlock(block);
            return;
        }

        Hash prevBlockHash = block.getPrevBlockHash();
        boolean orphaned = false;
        if (!blockDAO.hasBlock(prevBlockHash)) {
            orphaned = true;
        } else if (orphanedBlockDAO.isOrphanBlock(prevBlockHash)) {
            orphaned = true;
        }

        if (orphaned) {
            orphanedBlockDAO.addOrphanBlock(prevBlockHash, block.getHash());
        } else {

            int level = blockLevelDAO.getBlockLevel(block.getPrevBlockHash());

            List<Set<Hash>> deOrphaned = OrphanUtil.gatherOrphanageTree(
                    block.getHash(), (el) -> orphanedBlockDAO.resolvedOrphanBlock(el));

            level++;

            for (Set<Hash> hashes : deOrphaned) {
                for (Hash hash : hashes) {
                    blockLevelDAO.assignLevel(hash, level);
                }

                level++;
            }

            Set<Hash> allHashes = deOrphaned
                    .stream()
                    .flatMap(Set::stream)
                    .collect(toSet());

            orphanedBlockDAO.removeOrphanBlocks(allHashes);


            allHashes.stream()
                    .map(blockDAO::getBlock)
                    .forEach(super::addBlock);
        }

    }

}
