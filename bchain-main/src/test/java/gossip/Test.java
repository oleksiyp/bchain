package gossip;

import gossip.message.Message;
import gossip.message.MessageType;
import gossip.registry.Registry;
import gossip.registry.RegistryMapping;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static final int PORT_START = 2000;

    Random rnd = new Random();
    int cnt = 0;

    Histogram hist = new Histogram(3);

    static int nMsgs;
    static int nSends;

    public void run() throws IOException {
        SocketShared shared = new SocketShared(Registry.emptyRegistry()
                .register(0x0, PingMessage.TYPE, PingMessage::new)
                .register(0x1, PongMessage.TYPE, PongMessage::new)
                .register(0x2, RandomWalkMessage.TYPE, RandomWalkMessage::new), 64);

        RegistryMapping<MessageType<Message>, Message> typeMapping = shared.getMessageTypes();


        List<SocketGossip> servers = new ArrayList<>();
        AtomicInteger portGen = new AtomicInteger(PORT_START);
        for (int i = 0; i < 10; i++) {
            SocketGossip gossip = new SocketGossip(shared, portGen::getAndIncrement,
                    (party, msg) -> {
                        if (msg instanceof PingMessage) {
                            PingMessage ping = (PingMessage) msg;

                            PongMessage pong = new PongMessage();
                            pong.port = ping.port;
                            party.send(pong);
                        } else if (msg instanceof PongMessage) {

                            RandomWalkMessage rwm = typeMapping.create(RandomWalkMessage.TYPE);
                            rwm.setHops(10000);
                            rwm.setT(System.nanoTime());
                            for (int j = 0; j < 100; j++) {
                                if (nMsgs == 0) {
                                    party.send(rwm);
                                    nMsgs++;
                                }
                            }

                            typeMapping.reuse(RandomWalkMessage.TYPE, rwm);
                        } else if (msg instanceof RandomWalkMessage) {
                            nSends++;
                            SocketParty nextConn = party.getGossip().randomConnection();
                            RandomWalkMessage rwm = (RandomWalkMessage) msg;
                            long prevT = rwm.getT();
                            long nextT = System.nanoTime();
                            rwm.setT(nextT);

                            int hops = rwm.hops;
                            long mks = (nextT - prevT) / 1000;
                            hist.recordValue(mks);

                            if (hops > 0) {
                                rwm.hops = hops - 1;
                                nextConn.send(msg);
                            } else {
//                System.out.println("Zero hops " + ++cnt);
                                cnt++;
                                if (cnt == nMsgs) {
                                    for (double j = 50; j <= 100; j += 2) {
                                        System.out.println(j + "% " + hist.getValueAtPercentile(j));
                                    }
                                    shared.done = true;
                                    System.out.println(nSends + " messages");
                                }
                            }

                            typeMapping.reuse(RandomWalkMessage.TYPE, rwm);
                        }
                    });
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
    }


    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();

        new Test().run();

        long end = System.nanoTime();
        System.out.printf("%.3f seconds%n", (end - start) / 1e9);
    }
}
