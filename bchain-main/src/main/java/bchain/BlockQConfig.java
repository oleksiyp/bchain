package bchain;

import bchain.app.BlockAcceptor;
import bchain.domain.Block;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
public class BlockQConfig {
    @Bean
    public BlockQ blockQ() {
        return new BlockQ();
    }
}
