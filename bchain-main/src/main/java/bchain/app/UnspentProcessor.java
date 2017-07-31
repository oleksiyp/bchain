package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockLevelDao;
import bchain.dao.UnspentDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Predicate;

import static bchain.app.result.Result.*;

public class UnspentProcessor {
    @Autowired
    UnspentDao unspentDao;

    @Autowired
    BlockLevelDao blockLevelDao;

    @Autowired
    BranchSwitcher branchSwitcher;

    public Result process(Block newBlock, Predicate<Block> validator) {
        Hash head = unspentDao.getHead();
        if (head == null || newBlock.isGenesis()) {
            return genesis(head, newBlock, validator);
        }

        assignLevel(newBlock);

        return branchSwitcher.switchBranch(head, newBlock.getHash(),

                popedBlock -> {
                    pushPop(popedBlock, false);
                    unspentDao.setHead(popedBlock.getPrevBlockHash());
                    return ok();
                },

                pushedBlock -> {
                    if (pushedBlock.getHash().equals(newBlock.getHash())) {
                        if (!validator.test(newBlock)) {
                            return validationFailed();
                        }
                    }
                    pushPop(pushedBlock, true);
                    unspentDao.setHead(pushedBlock.getHash());
                    return ok();
                });
    }

    private Result genesis(Hash head, Block newBlock, Predicate<Block> validator) {
        if (head == null && newBlock.isGenesis()) {
            if (!validator.test(newBlock)) {
                return validationFailed();
            }
            pushPop(newBlock, true);
            unspentDao.setHead(newBlock.getHash());
            blockLevelDao.setLevel(newBlock.getHash(), 0);
            return ok();
        }

        return genesisFailed();
    }

    private void assignLevel(Block block) {
        int level = blockLevelDao.getLevel(block.getPrevBlockHash());

        level++;

        blockLevelDao.setLevel(block.getHash(), level);
    }

    private void pushPop(Block block, boolean push) {
        for (Tx tx : block.getTxs()) {
            List<TxOutput> outputs = tx.getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                TxOutput output = outputs.get(i);
                if (push) {
                    unspentDao.addTxOut(tx.getHash(), i);
                    unspentDao.changeUnspent(output.getAddress(), output.getValue());
                } else {
                    unspentDao.removeTxOut(tx.getHash(), i);
                    unspentDao.changeUnspent(output.getAddress(), -output.getValue());
                }
            }
        }
    }
}
