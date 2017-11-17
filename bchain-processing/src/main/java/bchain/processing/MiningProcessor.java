package bchain.processing;

import bchain.domain.Result;
import bchain.dao.PendingTxDao;
import bchain.dao.RefsDao;
import bchain.domain.Block;
import bchain.domain.BlockBuilder;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static bchain.domain.Result.nextNounce;
import static bchain.domain.Result.ok;
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
            Result lastMiningResult = ok();
            while (!interrupted()) {
                lock.lock();
                try {
                    while (!((!lastMiningResult.isOk() || recentVersion > version)
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

                lastMiningResult = mine(baseHash, pendingTxs);
            }
        } catch (InterruptedException ex) {

        }
    }

    @LogExecutionTime
    private Result mine(Hash baseHash, List<Tx> pendingTxs) {
        BlockBuilder builder = Block.builder();
        builder.setNounce(rndBytes(16));
        builder.setPrevBlockHash(baseHash);
        for (Tx tx : pendingTxs) {
            builder.add(tx);
        }

        Block newBlock = builder.build();

        if (!blockAcceptor.accept(newBlock)) {
            return nextNounce();
        }

        log.info("Mined {} {} txs based on {}", newBlock.getHash(), pendingTxs.size(), baseHash);

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
