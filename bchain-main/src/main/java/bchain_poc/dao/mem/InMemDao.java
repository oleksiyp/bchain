package bchain_poc.dao.mem;

import bchain_poc.dao.*;
import bchain_poc.domain.Block;
import bchain_poc.domain.Hash;
import bchain_poc.domain.PubKey;
import bchain_poc.domain.Tx;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

public class InMemDao
        implements BlockDAO,
        BlockLevelDAO,
        ChainDAO,
        OrphanedBlockDAO,
        OrphanedTxDAO,
        PendingTxDAO,
        RefsDAO,
        TxDAO,
        UnspentDAO,
        UnspentTxOutputsDAO {

    private Map<Hash, Tx> txStorage;
    private Map<Hash, Block> blockStorage;
    private Set<Hash> pendingTx;
    private Map<Hash, Integer> levelMap;
    private Map<PubKey, Long> unspent;
    private Hash head;
    private Hash master;

    public InMemDao() {
        txStorage = new HashMap<>();
        pendingTx = new HashSet<>();
        levelMap = new HashMap<>();
        unspent = new HashMap<>();
        blockStorage = new HashMap<>();
    }

    @Override
    public boolean hasTx(Hash hash) {
        return txStorage.containsKey(hash);
    }

    @Override
    public void addTx(Tx tx) {
        txStorage.put(tx.getHash(), tx);
    }

    @Override
    public Tx getTx(Hash hash) {
        return txStorage.get(hash);
    }


    @Override
    public List<Tx> getAllTx(List<Hash> txs) {
        return txs.stream()
                .map(this::getTx)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasBlock(Hash hash) {
        return blockStorage.containsKey(hash);
    }

    @Override
    public Block getBlock(Hash hash) {
        return blockStorage.get(hash);
    }

    @Override
    public void addBlock(Block block) {
        blockStorage.put(block.getHash(), block);
    }

    @Override
    public Hash parentBlockHash(Hash hash) {
        return blockStorage.get(hash).getPrevBlockHash();
    }



    @Override
    public Hash getHead() {
        return head;
    }

    @Override
    public void prepareHeadOp(Hash newHash, OpType push) {

    }

    @Override
    public void assignHead(Hash newHead) {
        head = newHead;
    }

    @Override
    public Hash getMaster() {
        return master;
    }

    @Override
    public void prepareMasterSwitch(Hash masterHash) {

    }

    @Override
    public void prepareMasterOp(Hash newHash, OpType opType) {

    }

    @Override
    public void assignMaster(Hash master) {
        this.master = master;
    }

    @Override
    public int getBlockLevel(Hash hash) {
        return levelMap.getOrDefault(hash, -1);
    }

    @Override
    public void assignLevel(Hash hash, int level) {
        levelMap.put(hash, level);
    }

    @Override
    public void markPending(Hash txHash, boolean add) {
        if (add) {
            pendingTx.add(txHash);
        } else {
            pendingTx.remove(txHash);
        }
    }

    @Override
    public void unspentPrepareAddRemove(PubKey address, boolean add, long value) {
    }

    @Override
    public void unspentCommitAddRemove(PubKey address, boolean add, long value, boolean rollback) {
        if (!rollback) {
            long val = unspent.getOrDefault(address, 0L);
            val += add ? value : -value;
            unspent.put(address, val);
            System.out.println(address + "=" + val);
        }
    }

    @Override
    public boolean unspentOutAddRemove(boolean add, Hash outTxHash, int n) {
        return false;
    }

    @Override
    public boolean isOrphanBlock(Hash hash) {
        return false;
    }

    @Override
    public boolean isOrphanTx(Hash txHash) {
        return false;
    }

    @Override
    public void addAll(Multimap<Hash, Hash> hash) {

    }

    @Override
    public Set<Hash> resolvedOrphanTx(Hash hash) {
        return new HashSet<>();
    }

    @Override
    public void removeOrphanTxs(Set<Hash> deOrphaned) {

    }

    @Override
    public void addOrphanBlock(Hash from, Hash to) {

    }

    @Override
    public Set<Hash> resolvedOrphanBlock(Hash hash) {
        return new HashSet<>();
    }

    @Override
    public void removeOrphanBlocks(Set<Hash> hashes) {

    }

    @Override
    public void declare(Hash hash, BlockType invalid) {

    }

}
