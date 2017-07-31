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

    }

    public void processFurther(Block block) {

    }
}
