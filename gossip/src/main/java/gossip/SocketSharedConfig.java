package gossip;

import gossip.registry.Registry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SocketSharedConfig {
    @Value("${socket.shared.max_in_msg_size:1048576}")
    public int maxInMsgSize;

    @Bean
    public SocketShared socketShared(@Qualifier("gossipMessageRegistry") Registry<?> registry) {
        try {
            return new SocketShared(registry, maxInMsgSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
