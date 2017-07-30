package bchain;

import bchain.app.StoreBlockProcessor;
import bchain.app.StoreTxProcessor;
import bchain.domain.Block;
import bchain.domain.PubKey;
import bchain.domain.Tx;
import bchain.domain.TxOutput;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxOutput.output;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    @Bean
    public StoreBlockProcessor storeBlockProcessor() {
        return new StoreBlockProcessor();
    }

    @Bean
    public StoreTxProcessor storeTxProcessor() {
        return new StoreTxProcessor();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        Block block = Block.builder()
                .add(Tx.builder()
                        .setCoinbase(true)
                        .add(output(pubKey(new byte[1], new byte[1]), 2000))
                        .build())
                .build();

        ctx.getBean(StoreBlockProcessor.class)
                .addBlock(block);
    }
}