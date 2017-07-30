package bchain.app;

import bchain.dao.TxDao;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static bchain.app.Result.containsSame;
import static bchain.app.Result.ok;
import static bchain.app.Result.verificationFailed;

public class StoreTxProcessor {
    @Autowired
    TxDao txDao;

    @Transactional
    public Result addTransaction(Tx tx) {
        if (!tx.verify()) {
            return verificationFailed();
        }

        if (txDao.hasTx(tx.getHash())) {
            return containsSame();
        }

        txDao.saveTx(tx);

        return ok();
    }
}
