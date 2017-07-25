package node2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static node2.registry.Registry.emptyRegistry;

public class Test {
    public static final int PORT_START = 2000;

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();

        SocketShared shared = new SocketShared(emptyRegistry()
                .register(0x0, PingMessage.TYPE, PingMessage::new)
                .register(0x1, PongMessage.TYPE, PongMessage::new)
                .register(0x2, RandomWalkMessage.TYPE, RandomWalkMessage::new), 64);

        List<SocketGossip> servers = new ArrayList<>();
        AtomicInteger portGen = new AtomicInteger(PORT_START);
        for (int i = 0; i < 10; i++) {
            SocketGossip gossip = new SocketGossip(shared, portGen::getAndIncrement);
            servers.add(gossip);
        }

        for (int i = 0; i < servers.size(); i++) {
            for (int j = i + 1; j < servers.size(); j++) {
                SocketGossip from = servers.get(i);
                SocketGossip to = servers.get(j);
                from.connect(new InetSocketAddress(to.getPort()));
            }
        }

        shared.loopSelector();

        long end = System.nanoTime();
        System.out.printf("%.3f seconds%n", (end - start) / 1e9);
    }
}
