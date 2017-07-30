package bchain_mini;

import java.util.*;

public class TxHandler {

    private final UTXOPool pool;

    /**
     * Creates a public ledger whose current bchain_mini.UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the bchain_mini.UTXOPool(bchain_mini.UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current bchain_mini.UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no bchain_mini.UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        Set<UTXO> utxos = new HashSet<>();

        double outputSum = 0;
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO utxo = new UTXO(tx.getHash(), i);
            if (!utxos.add(utxo)) {
                return false; // 3
            }

            double value = tx.getOutput(i).value;
            if (value < 0) {
                return false; // 4
            }
            outputSum += value;
        }

        double inputSum = 0;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!utxos.add(utxo)) {
                return false; // 3
            }

            Transaction.Output referencedOut = pool.getTxOutput(utxo);
            if (referencedOut == null) {
                return false; // 1
            }

            if (!Crypto.verifySignature(
                    referencedOut.address,
                    tx.getRawDataToSign(i),
                    input.signature)) {
                return false; // 2
            }

            inputSum += referencedOut.value;
        }

        return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current bchain_mini.UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        possibleTxs = sort(possibleTxs);
        List<Transaction> resultList = new ArrayList<>();
        Set<UTXO> doubleSpendSet = new HashSet<>();
        for (Transaction transaction : possibleTxs) {
            if (isValidTx(transaction)) {

                if (isDoubleSpend(transaction, doubleSpendSet)) {
                    continue;
                }

                for (int i = 0; i < transaction.numOutputs(); i++) {
                    UTXO utxo = new UTXO(transaction.getHash(), i);
                    pool.addUTXO(utxo, transaction.getOutput(i));
                }
                resultList.add(transaction);
            }
        }
        return resultList.toArray(new Transaction[resultList.size()]);
    }

    private Transaction[] sort(Transaction[] possibleTxs) {
        Transaction[] ret = new Transaction[possibleTxs.length];
        for (int i = 0; i < ret.length; i++) {
            int retJ = -1;
            for (int j = 0; j < ret.length; j++) {
                if (possibleTxs[j] == null) continue;
                retJ = j;
                next: for (int k = 0; k < ret.length; k++) {
                    if (possibleTxs[k] == null) continue;
                    for (Transaction.Input in : possibleTxs[k].getInputs()) {
                        if (Arrays.equals(in.prevTxHash, possibleTxs[j].getHash())) {
                            retJ = -1;
                            break next;
                        }
                    }
                }
            }
            if (retJ != -1) {
                ret[i] = possibleTxs[retJ];
                possibleTxs[retJ] = null;
            }
        }
        return ret;
    }

    private boolean isDoubleSpend(Transaction transaction, Set<UTXO> doubleSpendPool) {
        for (int i = 0; i < transaction.numInputs(); i++) {
            Transaction.Input input = transaction.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!doubleSpendPool.add(utxo)) {
                return true;
            }
        }
        return false;
    }

    public UTXOPool getUTXOPool() {
        return pool;
    }
}
