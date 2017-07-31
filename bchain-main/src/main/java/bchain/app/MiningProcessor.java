package bchain.app;

import bchain.app.result.Result;
import bchain.dao.PendingTxDao;
import bchain.dao.TxDao;
import bchain.domain.Block;
import bchain.domain.BlockBuilder;
import bchain.domain.Hash;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Predicate;

import static bchain.app.result.Result.nextNounce;
import static bchain.app.result.Result.ok;
import static bchain.util.RndUtil.rndBytes;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.Thread.interrupted;

public class MiningProcessor implements Runnable {
    @Autowired
    TxDao txDao;

    @Autowired
    PendingTxDao pendingTxDao;

    volatile Hash baseHash;
    volatile List<Tx> pendingTxs;
    volatile boolean breakMining;

    Predicate<Block> blockAcceptor;

    @Override
    public void run() {
        while (!interrupted()) {
            while (!breakMining) {
                Result miningResult = mine(baseHash,
                        pendingTxs,
                        rndBytes(16));
                if (miningResult.isOk()) {
                    // wait
                }
            }
            breakMining = false;
        }
    }

    private Result mine(Hash baseHash, List<Tx> pendingTxs, byte[] bytes) {
        BlockBuilder builder = Block.builder();
        for (Tx tx : pendingTxs) {
            builder.add(tx);
        }

        Block newBlock = builder.build();

        if (!blockAcceptor.test(newBlock)) {
            return nextNounce();
        }
        return ok();
    }

    public Result process(Tx tx) {
        pendingTxs = copyOf(txDao.allWith(pendingTxDao.all()));
        breakMining = true;
        return ok();
    }

    public Result process(Block block) {
        baseHash = block.getHash();
        breakMining = true;
        return ok();
    }
}
