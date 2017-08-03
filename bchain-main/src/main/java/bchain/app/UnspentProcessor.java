package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockLevelDao;
import bchain.dao.RefsDao;
import bchain.dao.TxDao;
import bchain.dao.UnspentDao;
import bchain.domain.*;
import bchain.util.GraphUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static bchain.app.result.Result.*;
import static java.util.function.Function.identity;

@Slf4j
public class UnspentProcessor {
    @Autowired
    UnspentDao unspentDao;

    @Autowired
    RefsDao refsDao;

    @Autowired
    BlockLevelDao blockLevelDao;

    @Autowired
    TxDao txDao;

    @Autowired
    BranchSwitcher branchSwitcher;

    public Result process(Block newBlock, Predicate<Block> validator) {
        Hash head = refsDao.getHead();
        if (head == null || newBlock.isGenesis()) {
            return genesis(head, newBlock, validator);
        }

        assignLevel(newBlock);

        return branchSwitcher.switchBranch(head, newBlock.getHash(),

                popedBlock -> {
                    pushPop(popedBlock, false);
                    refsDao.setHead(popedBlock.getPrevBlockHash());
                    return ok();
                },

                pushedBlock -> {
                    if (pushedBlock.getHash().equals(newBlock.getHash())) {
                        if (!validator.test(newBlock)) {
                            return validationFailed();
                        }
                    }
                    pushPop(pushedBlock, true);
                    refsDao.setHead(pushedBlock.getHash());
                    return ok();
                });
    }

    private Result genesis(Hash head, Block newBlock, Predicate<Block> validator) {
        if (head == null && newBlock.isGenesis()) {
            if (!validator.test(newBlock)) {
                return validationFailed();
            }
            pushPop(newBlock, true);
            refsDao.setHead(newBlock.getHash());
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
        Map<Hash, Tx> referencedTx = referencedTxs(block);
        List<Tx> txList = GraphUtil.topologicalSort(block.getTxs());

        Set<UnspentTxOut> unspentTxOuts = new HashSet<>();
        Set<UnspentTxOut> removeUnspentTxOuts = new HashSet<>();
        StopWatch sw = new StopWatch();
        for (Tx tx : txList) {
            sw.start(tx.getHash().toString());
            log.info("{}", tx.getHash());
            for (TxInput input : tx.getInputs()) {

                Hash prevTxHash = input.getPrevTxHash();
                Tx prevTx = referencedTx.get(prevTxHash);

                int n = input.getOutputIndex();
                TxOutput output = prevTx.getOutputs().get(n);

                UnspentTxOut unspentTxOut = new UnspentTxOut(prevTxHash, n, output.getAddress(), output.getValue());

                spendUnspend(unspentTxOuts, removeUnspentTxOuts, unspentTxOut, !push);
            }

            List<TxOutput> outputs = tx.getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                TxOutput output = outputs.get(i);

                UnspentTxOut unspentTxOut = new UnspentTxOut(tx.getHash(), i, output.getAddress(), output.getValue());

                spendUnspend(unspentTxOuts, removeUnspentTxOuts, unspentTxOut, push);
            }
            sw.stop();
        }

        sw.start("update");
        unspentDao.spendUnspend(
                new ArrayList<>(unspentTxOuts),
                new ArrayList<>(removeUnspentTxOuts));
        sw.stop();

        log.info("{}", sw);
    }

    private Map<Hash, Tx> referencedTxs(Block block) {
        HashSet<Hash> hashes = new HashSet<>();
        for (Tx tx : block.getTxs()) {
            List<TxInput> inputs = tx.getInputs();
            for (TxInput input : inputs) {
                hashes.add(input.getPrevTxHash());
            }
        }
        return txDao.allWith(new ArrayList<>(hashes))
                .stream()
                .collect(Collectors.toMap(Tx::getHash, identity()));
    }

    private void spendUnspend(Set<UnspentTxOut> unspentTxOuts, Set<UnspentTxOut> removeUnspentTxOuts, UnspentTxOut unspentTxOut, boolean unspend) {
        if (unspend) {
            if (!removeUnspentTxOuts.remove(unspentTxOut)) {
                unspentTxOuts.add(unspentTxOut);
            }
            log.info("Unspent +{} for [{}]", new Object[] { unspentTxOut.getValue(), unspentTxOut.getAddress()});
        } else {
            if (!unspentTxOuts.remove(unspentTxOut)) {
                removeUnspentTxOuts.add(unspentTxOut);
            }
            log.info("Spent -{} for [{}]", new Object[] { unspentTxOut.getValue(), unspentTxOut.getAddress()});
        }
    }
}
