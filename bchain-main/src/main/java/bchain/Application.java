package bchain;

import bchain.app.Processor;
import bchain.domain.*;
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
public class Application {

    @Autowired
    Processor processor;

    @Autowired
    BlockQ blockQ;

    private void run() {
        Hash prev = null;
        while (true) {
            Block block;
            while ((block = blockQ.blocks.poll()) != null) {
                System.out.println(block.getHash() + " " + block.getPrevBlockHash());
                for (Tx tx : block.getTxs()) {
                    System.out.println(tx);
                }
                processor.process(block);
            }
            TxBuilder builder = Tx.builder();
            if (prev != null) {
                builder.add(input(prev, 0, rndBytes(10)));
            } else {
                builder.setCoinbase(true);
            }
            builder.add(output(pubKey(rndBytes(10), rndBytes(10)), 20));
            Tx tx = builder.build();
            prev = tx.getHash();

            processor.process(tx);
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        ctx.getBean(Application.class)
                .run();
    }
}