package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockDao;
import bchain.dao.OrphanedBlockDao;
import bchain.dao.OrphanedTxDao;
import bchain.dao.TxDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.orphaned;

public class OrphanedProcessor {
    @Autowired
    TxDao txDao;

    @Autowired
    OrphanedTxDao orphanedTxDao;

    @Autowired
    BlockDao blockDao;

    @Autowired
    OrphanedBlockDao orphanedBlockDao;

    public Result process(Tx tx) {
        if (checkOrphaned(tx)) {
            orphanedTxDao.add(tx.getHash());
            return orphaned();
        }

        return ok();
    }

    public Result process(Block block) {
        if (checkOrphaned(block)) {
            orphanedBlockDao.add(block.getHash());
            return orphaned();
        }

        return ok();
    }

    public void deOrphan(Tx tx,
                         Block block,
                         Consumer<Tx> txConsumer,
                         Consumer<Block> blockConsumer) {
        Queue<Tx> txQueue = new ArrayDeque<>();
        Queue<Block> blockQueue = new ArrayDeque<>();
        if (tx != null) {
            txQueue.add(tx);
        }
        if (block != null) {
            blockQueue.add(block);

            block.getTxs()
                    .stream()
                    .filter(this::canBeDeOrphaned)
                    .forEach(txQueue::add);
        }

        while (!txQueue.isEmpty() || !blockQueue.isEmpty()) {
            while (!txQueue.isEmpty()) {
                tx = txQueue.remove();
                orphanedTxDao.remove(tx.getHash());
                txConsumer.accept(tx);

                txDao.referencingTxs(tx.getHash())
                        .stream()
                        .filter(this::canBeDeOrphaned)
                        .forEach(txQueue::add);

                blockDao.referencingBlocksByTx(tx.getHash())
                        .stream()
                        .filter(this::canBeDeOrphaned)
                        .forEach(blockQueue::add);
            }

            while (!blockQueue.isEmpty()) {
                block  = blockQueue.remove();
                orphanedBlockDao.remove(block.getHash());
                blockConsumer.accept(block);

                blockDao.referencingBlocksByBlock(block.getHash())
                        .stream()
                        .filter(this::canBeDeOrphaned)
                        .forEach(blockQueue::add);
            }
        }
    }

    private boolean canBeDeOrphaned(Tx tx) {
        return orphanedTxDao.isOrphaned(tx.getHash())
                && !this.checkOrphaned(tx);
    }

    private boolean canBeDeOrphaned(Block block) {
        return orphanedBlockDao.isOrphaned(block.getHash())
                && !this.checkOrphaned(block);
    }

    private boolean checkOrphaned(Block block) {
        Set<Hash> referencedTxs = block.getTxs()
                .stream()
                .map(Tx::getHash)
                .collect(Collectors.toSet());

        return orphanedTxDao.isOrphanedAny(referencedTxs) ||
                !txDao.hasAll(referencedTxs) ||
                orphanedBlockDao.isOrphaned(block.getPrevBlockHash()) ||
                !blockDao.hasBlock(block.getPrevBlockHash());
    }

    private boolean checkOrphaned(Tx tx) {
        Set<Hash> referencedTxs = tx.getInputs()
                .stream()
                .map(TxInput::getPrevTxHash)
                .collect(Collectors.toSet());

        return orphanedTxDao.isOrphanedAny(referencedTxs) ||
                !txDao.hasAll(referencedTxs);
    }
}
