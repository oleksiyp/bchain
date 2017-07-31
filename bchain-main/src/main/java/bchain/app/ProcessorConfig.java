package bchain.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessorConfig {
    @Bean
    public StoreTxProcessor storeTxProcessor() {
        return new StoreTxProcessor();
    }

    @Bean
    public StoreBlockProcessor storeBlockProcessor() {
        return new StoreBlockProcessor();
    }

    @Bean
    public OrphanedProcessor orphanedTxProcessor() {
        return new OrphanedProcessor();
    }
}
