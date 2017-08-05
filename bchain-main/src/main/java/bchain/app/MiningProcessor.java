package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockLevelDao;
import bchain.dao.PendingTxDao;
import bchain.dao.RefsDao;
import bchain.dao.TxDao;
import bchain.domain.Block;
import bchain.domain.BlockBuilder;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import com.google.common.collect.ImmutableList;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static bchain.app.result.Result.nextNounce;
import static bchain.app.result.Result.ok;
import static bchain.util.RndUtil.rndBytes;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.Thread.interrupted;

@Slf4j
public class MiningProcessor implements Runnable {
    @Autowired
    PendingTxDao pendingTxDao;

    @Autowired
    RefsDao refsDao;

    Lock lock = new ReentrantLock();
    Condition hasNewerData = lock.newCondition();

    Hash recentBaseHash;
    List<Tx> recentPendingTxs;
    long recentVersion;

    @Autowired
    BlockAcceptor blockAcceptor;

    @Override
    public void run() {
        try {
            long version = 0;
            Hash baseHash;
            List<Tx> pendingTxs;
            while (!interrupted()) {
                lock.lock();
                try {
                    while (!(recentVersion > version
                            && recentPendingTxs != null
                            && !recentPendingTxs.isEmpty())) {
                        hasNewerData.await();
                    }

                    version = recentVersion;
                    pendingTxs = recentPendingTxs;
                    baseHash = recentBaseHash;
                } finally {
                    lock.unlock();
                }

                log.info("Mining {} txs based on {}", pendingTxs.size(), baseHash);
                Result miningResult = mine(baseHash,
                        pendingTxs,
                        rndBytes(16));

                miningResult.isOk();
            }
        } catch (InterruptedException ex) {

        }
    }

    @LogExecutionTime
    private Result mine(Hash baseHash, List<Tx> pendingTxs, byte[] nounce) {
        BlockBuilder builder = Block.builder();
//        builder.setNounce(nounce);
        builder.setPrevBlockHash(baseHash);
        for (Tx tx : pendingTxs) {
            builder.add(tx);
        }

        Block newBlock = builder.build();

        if (!blockAcceptor.accept(newBlock)) {
            return nextNounce();
        }
        return ok();
    }

    public Result updateMiningTarget() {
        Hash master = refsDao.getMaster();
        List<Tx> pendingTsx = copyOf(pendingTxDao.allTx());

        lock.lock();
        try {
            recentBaseHash = master;
            recentPendingTxs = pendingTsx;
            recentVersion++;
            hasNewerData.signal();
        } finally {
            lock.unlock();
        }
        return ok();
    }
}
