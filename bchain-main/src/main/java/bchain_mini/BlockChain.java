package bchain_mini;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

import static java.util.Arrays.asList;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private static class BlockAndPool {
        final Block block;
        final UTXOPool pool;

        BlockAndPool(Block block, UTXOPool pool) {
            this.block = block;
            this.pool = pool;
        }
    }


    private final List<List<BlockAndPool>> blocks;
    private TransactionPool transactionPool;
    private int baseHeight;
    private Map<ByteArrayWrapper, Integer> heightMap;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blocks = new ArrayList<>();
        heightMap = new HashMap<>();

        List<BlockAndPool> list = new ArrayList<>();
        UTXOPool pool = new UTXOPool();
        List<Transaction> txs = genesisBlock.getTransactions();
        for (int i = 0; i < txs.size(); i++) {
            Transaction tx = txs.get(i);
            for (int j = 0; j < tx.numOutputs(); j++) {
                UTXO utxo = new UTXO(tx.getHash(), j);
                pool.addUTXO(utxo, tx.getOutput(j));
            }
        }
        list.add(new BlockAndPool(genesisBlock, pool));
        blocks.add(list);


        heightMap.put(new ByteArrayWrapper(genesisBlock.getHash()), 0);

        transactionPool = new TransactionPool();
    }

    public int getMaxHeight() {
        return baseHeight + blocks.size();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return getMaxHeightBlockAndPool().block;
    }

    /** Get the bchain_mini.UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return getMaxHeightBlockAndPool().pool;
    }

    private BlockAndPool getMaxHeightBlockAndPool() {
        return this.blocks.get(this.blocks.size() - 1).get(0);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null) {
            return false;
        }

        Integer parentHeight = heightMap.get(new ByteArrayWrapper(prevBlockHash));
        if (parentHeight == null) {
            return false;
        }

        int parentHeightRel = parentHeight - baseHeight;
        if (!(0 <= parentHeightRel && parentHeightRel < blocks.size())) {
            return false;
        }

        List<BlockAndPool> blockList = blocks.get(parentHeightRel);
        Optional<BlockAndPool> parentBapOpt = blockList.stream()
                .filter(bap -> Arrays.equals(bap.block.getHash(), prevBlockHash))
                .findFirst();

        if (!parentBapOpt.isPresent()) {
            return false;
        }

        BlockAndPool parentBap = parentBapOpt.get();

        UTXOPool pool = new UTXOPool(parentBap.pool);

        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);

        Set<UTXO> doubleSpentSet = new HashSet<>();
        for (Transaction tx : txs) {
            for (int j = 0; j < tx.numInputs(); j++) {
                Transaction.Input input = tx.getInput(j);
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                if (!doubleSpentSet.add(utxo)) {
                    return false;
                }
            }
        }


        TxHandler handler = new TxHandler(pool);

        Transaction[] processed = handler.handleTxs(txs);
        block.getTransactions().clear();
        block.getTransactions().addAll(asList(processed));

        // okay then process and addAll it

        block.getTransactions().forEach(tx -> transactionPool.removeTransaction(tx.getHash()));

        int height = parentHeight + 1;
        int heightRel = height - baseHeight;

        if (CUT_OFF_AGE + 1 == heightRel) {
            blocks.get(0).forEach(bap ->
                heightMap.remove(new ByteArrayWrapper(bap.block.getHash())));
            blocks.remove(0);

            baseHeight++;
            heightRel--;
        }

        if (heightRel == blocks.size()) {
            blocks.add(new ArrayList<>());
        }

        blocks.get(heightRel).add(new BlockAndPool(block, pool));
        heightMap.put(new ByteArrayWrapper(block.getHash()), height);

        return true;
    }


    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }
}