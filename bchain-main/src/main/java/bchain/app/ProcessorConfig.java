package bchain.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessorConfig {
    @Bean
    public VerificationProcessor verificationProcessor() {
        return new VerificationProcessor();
    }

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

    @Bean
    public BranchSwitcher branchSwitcher() {
        return new BranchSwitcher();
    }

    @Bean
    public ValidationProcessor validationProcessor() {
        return new ValidationProcessor();
    }

    @Bean
    public UnspentProcessor unspentTxProcessor() {
        return new UnspentProcessor();
    }

    @Bean
    public ElectionProcessor electionProcessor() {
        return new ElectionProcessor();
    }

    @Bean
    public MiningProcessor miningProcessor() {
        MiningProcessor processor = new MiningProcessor();
        new Thread(processor,"miner").start();
        return processor;
    }

    @Bean
    public Processor processor() {
        return new Processor();
    }
}
