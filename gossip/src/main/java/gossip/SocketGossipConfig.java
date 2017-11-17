package gossip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SocketGossipConfig {
    @Value("${gossip.port}")
    int startPort;

    @Bean
    public SocketGossip socketGossip(SocketShared socketShared) {
        AtomicInteger port = new AtomicInteger(startPort);
        try {
            return new SocketGossip(socketShared, port::getAndIncrement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
