package bchain.app;

import bchain.app.result.Result;
import bchain.domain.Block;
import bchain.domain.Tx;
import bchain.util.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class BlockChainProcessor {
    @Autowired
    VerificationProcessor verificationProcessor;

    @Autowired
    StoreTxProcessor storeTxProcessor;

    @Autowired
    StoreBlockProcessor storeBlockProcessor;

    @Autowired
    OrphanedProcessor orphanedProcessor;

    @Autowired
    ValidationProcessor validationProcessor;

    @Autowired
    UnspentProcessor unspentProcessor;

    @Autowired
    ElectionProcessor electionProcessor;

    @Autowired
    MiningProcessor miningProcessor;

    @Transactional
    @LogExecutionTime
    public void process(Tx inTx) {
        Result result;
        result = verificationProcessor.verify(inTx);
        if (!result.isOk()) {
            log.warn("Error storing: {}", result.getMessage());
            return;
        }

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

    @Transactional
    @LogExecutionTime
    public Result process(Block inBlock) {
        Result result;
        result = verificationProcessor.verify(inBlock);
        if (!result.isOk()) {
            log.warn("Error storing: {}", result.getMessage());
            return result;
        }

        result = storeBlockProcessor.store(inBlock);
        if (!result.isOk()) {
            log.warn("Error storing: {}", result.getMessage());
            return result;
        }

        result = orphanedProcessor.process(inBlock);
        if (!result.isOk()) {
            log.warn("Error linking: {}", result.getMessage());
            return result;
        }

        orphanedProcessor.deOrphan(null,
                inBlock,
                this::processFurther,
                this::processFurther);
        return Result.ok();
    }

    public void processFurther(Tx tx) {
        Result result = electionProcessor.process(tx);
        if (!result.isOk()) {
            log.info("Error election processing: {}", result.getMessage());
            return;
        }

        result = miningProcessor.updateMiningTarget();
        if (!result.isOk()) {
            log.info("Error mining processing: {}", result.getMessage());
            return;
        }

    }

    public void processFurther(Block block) {
        Result result = unspentProcessor.process(block,
                validationProcessor::validate);
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

        result = miningProcessor.updateMiningTarget();
        if (!result.isOk()) {
            log.warn("Error mining processing: {}", result.getMessage());
        }
    }
}
