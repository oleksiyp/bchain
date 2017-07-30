package bchain_poc.processing.orphan;

import bchain_poc.Factory;
import bchain_poc.domain.Hash;
import bchain_poc.domain.Tx;
import bchain_poc.domain.TxInput;
import bchain_poc.processing.Processor;
import bchain_poc.dao.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class OrphanTxProcessor extends Processor {
    TxDAO txDAO;

    OrphanedTxDAO orphanedTxDAO;

    public OrphanTxProcessor(Factory factory, Processor next) {
        super(next);

        txDAO = factory.create(TxDAO.class);
        orphanedTxDAO = factory.create(OrphanedTxDAO.class);
    }

    @Override
    public void addTransaction(Tx tx) {
        List<TxInput> inputs = tx.getInputs();
        boolean orphanedTx = false;
        Multimap<Hash, Hash> references = HashMultimap.create();

        for (TxInput input : inputs) {
            Hash refTxHash = input.getOutputTxHash();
            if (!txDAO.hasTx(refTxHash)) {
                orphanedTx = true;
                references.put(refTxHash, tx.getHash());
            } else if (orphanedTxDAO.isOrphanTx(refTxHash)) {
                orphanedTx = true;
                references.put(refTxHash, tx.getHash());
            }
        }

        if (orphanedTx) {
            orphanedTxDAO.addAll(references);
        } else {
            Set<Hash> deOrphaned = OrphanUtil.gatherOrphanageTree(tx.getHash(),
                    (el) -> orphanedTxDAO.resolvedOrphanTx(el))
                    .stream()
                    .flatMap(Set::stream)
                    .collect(toSet());

            orphanedTxDAO.removeOrphanTxs(deOrphaned);
            for (Hash hash : deOrphaned) {
                super.addTransaction(txDAO.getTx(hash));
            }
        }
    }

}
