package bchain;

import bchain.app.Processor;
import bchain.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static bchain.util.RndUtil.rndBytes;

@EnableAutoConfiguration
@ComponentScan
@Slf4j
public class Application {

    @Autowired
    Processor processor;

    @Autowired
    BlockQ blockQ;

    private void run() {
        Hash prev = null;

        Tx base = Tx.builder()
                .setCoinbase(true)
                .add(output(pubKey(rndBytes(10), rndBytes(10)), 20))
                .build();

        Block genesisBlock = Block.builder()
                .add(base)
                .build();

        blockQ.blocks.add(genesisBlock);
        prev = base.getHash();

        while (true) {
            Block block;
            while ((block = blockQ.blocks.poll()) != null) {
                log.info("{} {}", block.getHash(), block.getPrevBlockHash());
                for (Tx tx : block.getTxs()) {
                    log.info("{}", tx);
                }
                processor.process(block);
            }
            Tx tx = Tx.builder()
                    .add(input(prev, 0, rndBytes(10)))
                    .add(output(pubKey(rndBytes(10), rndBytes(10)), 20))
                    .build();
            prev = tx.getHash();

            log.info("New tx {}", tx.getHash());
            processor.process(tx);
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        ctx.getBean(Application.class)
                .run();
    }
}