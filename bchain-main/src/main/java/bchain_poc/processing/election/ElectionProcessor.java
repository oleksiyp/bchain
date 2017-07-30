package bchain_poc.processing.election;

import bchain_poc.Factory;
import bchain_poc.dao.BlockLevelDAO;
import bchain_poc.dao.PendingTxDAO;
import bchain_poc.dao.RefsDAO;
import bchain_poc.domain.Block;
import bchain_poc.domain.Hash;
import bchain_poc.domain.Tx;
import bchain_poc.mining.Miner;
import bchain_poc.processing.Processor;
import bchain_poc.processing.path.MasterSwitcher;
import bchain_poc.processing.path.PathFinder;
import bchain_poc.processing.path.PathItem;

import java.util.List;

public class ElectionProcessor extends Processor {
    private final RefsDAO refsDAO;

    private final BlockLevelDAO levelDAO;

    private final PendingTxDAO pendingTxDAO;

    private final PathFinder pathFinder;

    private final MasterSwitcher pathSwitcher;

    private final Miner miner;

    public ElectionProcessor(Factory factory, Processor next) {
        super(next);
        refsDAO = factory.create(RefsDAO.class);
        levelDAO = factory.create(BlockLevelDAO.class);
        pendingTxDAO = factory.create(PendingTxDAO.class);
        pathFinder = factory.create(PathFinder.class);
        pathSwitcher = factory.create(MasterSwitcher.class);
        miner = factory.create(Miner.class);
    }

    @Override
    public void addTransaction(Tx tx) {
        pendingTxDAO.markPending(tx.getHash(), true);
        miner.pendingTx(tx.getHash(), true);
        super.addTransaction(tx);
    }

    @Override
    public void addBlock(Block block) {
        boolean switchMaster;

        Hash master;
        if (block.isGenesis()) {
            master = null;
        } else {
            master = refsDAO.getMaster();
            if (master == null) {
                return;
            }
        }

        if (master != null) {
            int blockLevel = levelDAO.getBlockLevel(block.getHash());
            int masterLevel = levelDAO.getBlockLevel(master);

            switchMaster = false;
            if (blockLevel > masterLevel) {
                switchMaster = true;
            } else if (blockLevel == masterLevel) {
                // TODO resolve this with other props
                switchMaster = true;
            }
        } else {
            switchMaster = true;
        }

        if (switchMaster) {
            List<PathItem> path = pathFinder.findPath(master, block.getHash());
            pathSwitcher.setPath(path);
            pathSwitcher.setProcessTx(this::processTx);
            pathSwitcher.perform();

            miner.masterSwitched(block.getHash());
        }
    }

    private void processTx(Tx tx, boolean add) {
        // if it's in block it's not pending
        pendingTxDAO.markPending(tx.getHash(), !add);
        miner.pendingTx(tx.getHash(), !add);
    }
}