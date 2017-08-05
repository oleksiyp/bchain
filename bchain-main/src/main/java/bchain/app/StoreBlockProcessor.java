package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockDao;
import bchain.domain.Block;
import bchain.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.app.result.Result.duplicated;
import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.verificationFailed;

public class StoreBlockProcessor {
    @Autowired
    BlockDao blockDao;

    @LogExecutionTime
    public Result store(Block block) {
        if (!blockDao.saveBlock(block)) {
            return duplicated();
        }

        return ok();
    }
}
