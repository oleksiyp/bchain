package bchain_poc.processing.receive;

import bchain_poc.Factory;
import bchain_poc.domain.Tx;
import bchain_poc.processing.Processor;
import bchain_poc.dao.TxDAO;

public class ReceiveTxProcessor extends Processor {
    private final TxDAO txDAO;

    public ReceiveTxProcessor(Factory factory, Processor next) {
        super(next);
        this.txDAO = factory.create(TxDAO.class);
    }

    public void addTransaction(Tx tx) {
        if (!tx.verify()) {
            return;
        }

        if (txDAO.hasTx(tx.getHash())) {
            return;
        }

        txDAO.addTx(tx);

        super.addTransaction(tx);
    }
}