package bchain.app;

import bchain.app.result.Result;
import bchain.dao.TxDao;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static bchain.app.result.Result.containsSame;
import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.verificationFailed;

public class StoreTxProcessor {
    @Autowired
    TxDao txDao;

    public Result store(Tx tx) {
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
