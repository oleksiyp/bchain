package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockLevelDao;
import bchain.dao.RefsDao;
import bchain.dao.PendingTxDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static bchain.app.result.Result.ok;
import static java.util.Collections.singletonList;

@Slf4j
public class ElectionProcessor {
    @Autowired
    RefsDao refsDao;

    @Autowired
    BlockLevelDao levelDao;

    @Autowired
    PendingTxDao pendingTxDao;

    @Autowired
    BranchSwitcher branchSwitcher;

    public Result process(Tx tx) {
        pendingTxDao.markPending(singletonList(tx.getHash()));
        return ok();
    }

    public Result process(Block block) {
        Hash master = refsDao.getMaster();

        if (master == null) {
            pendingTxDao.unmarkPending(
                    hashes(block.getTxs()));
            refsDao.setMaster(block.getHash());
            return ok();
        }

        int masterLevel = levelDao.getLevel(master);
        int level = levelDao.getLevel(block.getHash());

        if (masterLevel >= level) {
            return Result.NOT_ELECTED;
        }

        log.info("Switching master to {} at level {}", block.getHash(), level);
        return branchSwitcher.switchBranch(
                master, block.getHash(),

                popedBlock -> {
                    pendingTxDao.markPending(
                            hashes(popedBlock.getTxs()));
                    refsDao.setMaster(popedBlock.getPrevBlockHash());
                    return ok();
                },

                pushedBlock -> {
                    pendingTxDao.unmarkPending(
                            hashes(pushedBlock.getTxs()));
                    refsDao.setMaster(pushedBlock.getHash());
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
