package bchain.app;

import bchain.dao.BlockDao;
import bchain.domain.Block;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static bchain.app.Result.containsSame;
import static bchain.app.Result.ok;
import static bchain.app.Result.verificationFailed;

public class StoreBlockProcessor {
    @Autowired
    BlockDao blockDao;

    @Autowired
    StoreTxProcessor storeTxProcessor;

    @Transactional
    public Result addBlock(Block block) {
        if (!block.verify()) {
            return verificationFailed();
        }

        if (blockDao.hasBlock(block.getHash())) {
            return containsSame();
        }

        for (Tx tx : block.getTxs()) {
            storeTxProcessor.addTransaction(tx);
        }

        blockDao.saveBlock(block);

        return ok();
    }
}