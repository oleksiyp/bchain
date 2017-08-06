package bchain.processing;

import bchain.domain.Result;
import bchain.dao.TxDao;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.domain.Result.duplicated;
import static bchain.domain.Result.ok;

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
