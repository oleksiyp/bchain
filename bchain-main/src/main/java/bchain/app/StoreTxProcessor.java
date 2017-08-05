package bchain.app;

import bchain.app.result.Result;
import bchain.dao.TxDao;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.app.result.Result.duplicated;
import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.verificationFailed;

public class StoreTxProcessor {
    @Autowired
    TxDao txDao;

    @LogExecutionTime
    public Result store(Tx tx) {
        if (!tx.verify()) {
            return verificationFailed();
        }

        if (!txDao.saveTx(tx)) {
            return duplicated();
        }

        return ok();
    }
}
