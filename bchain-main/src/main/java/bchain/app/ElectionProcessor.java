package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockLevelDao;
import bchain.dao.MasterBlockDao;
import bchain.dao.PendingTxDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static bchain.app.result.Result.ok;
import static java.util.Collections.singletonList;

public class ElectionProcessor {
    @Autowired
    MasterBlockDao masterBlockDao;

    @Autowired
    BlockLevelDao levelDao;

    @Autowired
    PendingTxDao pendingTxDao;

    @Autowired
    BranchSwitcher branchSwitcher;

    public void process(Tx tx) {
        pendingTxDao.markPending(singletonList(tx.getHash()));
    }

    public Result process(Block block) {
        Hash master = masterBlockDao.getMaster();

        int masterLevel = levelDao.getLevel(master);
        int level = levelDao.getLevel(block.getHash());

        if (masterLevel <= level) {
            return Result.NOT_ELECTED;
        }

        return branchSwitcher.switchBranch(
                master, block.getHash(),

                popedBlock -> {
                    pendingTxDao.markPending(
                            hashes(popedBlock.getTxs()));
                    masterBlockDao.setMaster(popedBlock.getPrevBlockHash());
                    return ok();
                },

                pushedBlock -> {
                    pendingTxDao.unmarkPending(
                            hashes(pushedBlock.getTxs()));
                    masterBlockDao.setMaster(pushedBlock.getHash());
                    return ok();
                });
    }

    private List<Hash> hashes(List<Tx> txs) {
        return txs
                .stream()
                .map(Tx::getHash)
                .collect(Collectors.toList());
    }
}
