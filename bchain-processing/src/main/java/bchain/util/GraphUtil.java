package bchain.util;

import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.*;

public class GraphUtil {
    public static List<Tx> topologicalSort(List<Tx> txs) {
        // rewrite
        SetMultimap<Hash, Hash> edges = HashMultimap.create();
        SetMultimap<Hash, Hash> revEdges = HashMultimap.create();

        Map<Hash, Tx> allTxs = new HashMap<>();
        Set<Hash> nonReferred = new HashSet<>();

        for (Tx tx : txs) {
            allTxs.put(tx.getHash(), tx);
            nonReferred.add(tx.getHash());
        }

        for (Tx tx : txs) {
            List<TxInput> inputs = tx.getInputs();
            for (TxInput input : inputs) {
                if (allTxs.containsKey(input.getPrevTxHash())) {
                    edges.put(input.getPrevTxHash(), tx.getHash());
                    revEdges.put(tx.getHash(), input.getPrevTxHash());
                    nonReferred.remove(tx.getHash());
                }
            }
        }

        List<Tx> result = new ArrayList<>();
        while (!nonReferred.isEmpty()) {
            Iterator<Hash> it = nonReferred.iterator();
            Hash prevTxHash = it.next();
            Tx prevTx = allTxs.get(prevTxHash);
            it.remove();

            result.add(prevTx);

            for (Hash txHash : edges.get(prevTxHash)) {
                revEdges.remove(txHash, prevTxHash);
                if (revEdges.get(txHash).isEmpty()) {
                    nonReferred.add(txHash);
                }
            }
        }
        return result;
    }
}
