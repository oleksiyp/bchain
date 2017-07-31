package bchain.app;

import bchain.app.result.Result;
import bchain.domain.Block;
import bchain.domain.Tx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class Processor {
    @Autowired
    StoreTxProcessor storeTxProcessor;

    @Autowired
    StoreBlockProcessor storeBlockProcessor;

    @Autowired
    OrphanedProcessor orphanedProcessor;

    @Autowired
    UnspentProcessor unspentProcessor;

    @Autowired
    ElectionProcessor electionProcessor;

    @Autowired
    MiningProcessor miningProcessor;

    public void process(Tx inTx) {
        Result result;
        result = storeTxProcessor.store(inTx);
        if (!result.isOk()) {
            log.warn("Error storing: {}", result.getMessage());
            return;
        }

        result = orphanedProcessor.process(inTx);
        if (!result.isOk()) {
            log.warn("Error linking: {}", result.getMessage());
            return;
        }

        orphanedProcessor.deOrphan(inTx,
                null,
                this::processFurther,
                this::processFurther);
    }

    public void process(Block inBlock) {
        Result result;
        result = storeBlockProcessor.store(inBlock);
        if (!result.isOk()) {
            log.warn("Error storing: {}", result.getMessage());
            return;
        }

        result = orphanedProcessor.process(inBlock);
        if (!result.isOk()) {
            log.warn("Error linking: {}", result.getMessage());
            return;
        }

        orphanedProcessor.deOrphan(null,
                inBlock,
                this::processFurther,
                this::processFurther);
    }

    public void processFurther(Tx tx) {
        miningProcessor.process(tx);
    }

    public void processFurther(Block block) {
        Result result = unspentProcessor.process(block, this::validate);
        if (!result.isOk()) {
            log.warn("Error unspent processing: {}", result.getMessage());
            return;
        }

        result = electionProcessor.process(block);
        if (result == Result.NOT_ELECTED) {
            log.info("Not elected: {}", result.getMessage());
            return;
        } else if (!result.isOk()) {
            log.info("Error election processing: {}", result.getMessage());
            return;
        }

        result = miningProcessor.process(block);
        if (!result.isOk()) {
            log.warn("Error mining processing: {}", result.getMessage());
            return;
        }
    }

    private boolean validate(Block block) {
        return true;
    }
}
