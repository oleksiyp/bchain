package bchain.app;

import bchain.app.result.Result;
import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.app.result.Result.duplicated;
import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.verificationFailed;
import static bchain.domain.Crypto.computeHash;

public class StoreTxProcessor {
    @Autowired
    TxDao txDao;

    @LogExecutionTime
    public Result store(Tx tx) {
        if (!txDao.saveTx(tx)) {
            return duplicated();
        }

        return ok();
    }
}
