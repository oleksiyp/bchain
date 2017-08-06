package bchain.app;

import bchain.domain.Result;
import bchain.dao.BlockDao;
import bchain.domain.Block;
import bchain.util.LogExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.domain.Result.duplicated;
import static bchain.domain.Result.ok;

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
