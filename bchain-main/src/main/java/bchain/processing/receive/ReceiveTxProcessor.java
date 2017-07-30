package bchain.processing.receive;

import bchain.Factory;
import bchain.domain.Tx;
import bchain.processing.Processor;
import bchain.dao.TxDAO;

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
