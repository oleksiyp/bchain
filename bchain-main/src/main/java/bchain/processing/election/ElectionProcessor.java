package bchain.processing.election;

import bchain.Factory;
import bchain.dao.BlockLevelDAO;
import bchain.dao.PendingTxDAO;
import bchain.dao.RefsDAO;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.mining.Miner;
import bchain.processing.Processor;
import bchain.processing.path.MasterSwitcher;
import bchain.processing.path.PathFinder;
import bchain.processing.path.PathItem;

import java.util.ArrayList;
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