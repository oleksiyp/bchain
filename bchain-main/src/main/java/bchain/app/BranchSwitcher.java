package bchain.app;

import bchain.app.result.Result;
import bchain.dao.BlockDao;
import bchain.dao.BlockLevelDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static bchain.app.result.Result.consistencyProblem;
import static bchain.app.result.Result.ok;

@Slf4j
public class BranchSwitcher {
    @Autowired
    BlockDao blockDao;

    @Autowired
    BlockLevelDao blockLevelDao;

    public Result switchBranch(Hash from,
                               Hash to,
                               Function<Block, Result> pop,
                               Function<Block, Result> push) {

        int toLevel = blockLevelDao.getLevel(to);
        int fromLevel = blockLevelDao.getLevel(from);

        List<Block> toBePoped = new ArrayList<>();
        List<Block> toBePushed = new ArrayList<>();

        while (fromLevel > toLevel) {
            Block block = blockDao.get(from);
            toBePoped.add(block);
            from = block.getPrevBlockHash();
            fromLevel--;
        }

        while (toLevel > fromLevel) {
            Block block = blockDao.get(to);
            toBePushed.add(block);
            to = block.getPrevBlockHash();
            toLevel--;
        }

        while (!from.equals(to) && fromLevel >= 0 && toLevel >= 0) {
            Block block = blockDao.get(from);
            toBePoped.add(block);
            from = block.getPrevBlockHash();
            fromLevel--;

            block = blockDao.get(to);
            toBePushed.add(block);
            to = block.getPrevBlockHash();
            toLevel--;
        }

        if (!from.equals(to)) {
            return consistencyProblem();
        }

        for (Block block : toBePoped) {
            Result res = pop.apply(block);
            if (!res.isOk()) {
                return res;
            }
        }

        Collections.reverse(toBePushed);

        for (Block block : toBePushed) {
            Result res = push.apply(block);
            if (!res.isOk()) {
                return res;
            }
        }

        return ok();
    }
}
