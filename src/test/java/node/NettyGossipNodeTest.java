package node;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import kryo.KryoObjectPool;
import node.ledger.Ledger;
import node.ledger.LedgerDispatcher;
import node.ledger.LedgerMonitor;
import node.netty.NettyGossipNode;
import node.netty.NettyPools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Cancelable;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.net.InetAddress.getByName;
import static java.util.Comparator.comparing;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class NettyGossipNodeTest {
    public static final Logger LOGGER = LogManager.getLogger(NettyGossipNodeTest.class);
    InetAddress localhost;
    MessageDigest md5;

    Random rnd;
    List<Gossip> gossips;
    List<Cancelable> cancellables;

    int port;
    NettyPools pools;
    ScheduledExecutorService scheduledExecutor;
    private Map<Gossip, Integer> rndId = new HashMap<>();
    private DefaultThreadFactory ledgerThreadFactory;
    private LedgerDispatcher dispatcher;
    private KryoObjectPool pool;


    static class Runner {
        private Runnable runnable;

        public Runnable getRunnable() {
            return runnable;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }
    }

    @Before
    public void setUp() throws Exception {
        port = 2000;

        rnd = new Random(555);
        gossips = new ArrayList<>();
        cancellables = new ArrayList<>();
        localhost = getByName("localhost");
        md5 = MessageDigest.getInstance("md5");

        pools = new NettyPools();
        cancellables.add(pools);

        ledgerThreadFactory = new DefaultThreadFactory("ledger");
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        cancellables.add(scheduledExecutor::shutdown);

        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        pool = new KryoObjectPool();

        dispatcher = new LedgerDispatcher(
                1024 * 1024,
                ledgerThreadFactory,
                2);

        dispatcher.start();

        new LedgerMonitor(dispatcher);
    }

    @After
    public void tearDown() throws Exception {
        Collections.reverse(cancellables);
        cancellables.forEach(Cancelable::cancel);
    }

    @Test
    public void send100MessagesOn100Nodes() throws Exception {
        int nNodes = 100;
        int nMessages = 10000;

        CompletableFuture<Void> future = new CompletableFuture<>();

        AtomicInteger nodesComplete = new AtomicInteger();
        for (int i = 0; i < nNodes; i++) {
            AtomicInteger messagesComplete = new AtomicInteger();
            Gossip node = createNode();
            listen(node, MyMessage.class, (msg) -> {
                if (messagesComplete.incrementAndGet() == nMessages) {
                    if (nodesComplete.incrementAndGet() == nNodes) {
                        future.complete(null);
                    }
                }
            });
        }

//        List<AutoDiscovery2> autoDiscoveries = gossips.stream()
//                .map(node -> new AutoDiscovery2(node))
//                .peek(AutoDiscovery2::start)
//                .collect(Collectors.toList());
//
//        SECONDS.sleep(10000);

//        CompletableFuture<Void> doneFuture = new CompletableFuture<>();
//        AtomicInteger done = new AtomicInteger();
//        List<AutoDiscovery> autoDiscoveries = gossips.stream()
//                .map(node ->
//                        new AutoDiscovery(node,
//                                rnd,
//                                scheduledExecutor,
//                                3,
//                                10,
//                                0,
//                                10,
//                                md5,
//                                () -> {
//                                    if (done.incrementAndGet() == gossips.size()) {
//                                        doneFuture.complete(null);
//                                    }
//                                }))
//                .peek(AutoDiscovery::start)
//                .collect(Collectors.toList());
//
//        try {
//            doneFuture.get(500, SECONDS);
//        } catch (TimeoutException ex) {
//            System.out.println(
//                    autoDiscoveries.stream()
//                            .map(c -> c.getGossip().nChannels())
//                            .collect(Collectors.toList()));
//            throw ex;
//        }

        out.println(gossips
                .stream()
                .map(Gossip::nChannels)
                .collect(Collectors.toList()));

        for (int i = 0; i < nMessages; i++) {
            gossips
                    .get(i % gossips.size())
                    .send(new MyMessage());
        }

        future.get(90, SECONDS);


    }

    private NettyGossipNode createNode() {
        Ledger ledger = new Ledger(
                100000,
                HOURS.toMillis(100),
                dispatcher);

        NettyGossipNode newNode = new NettyGossipNode(
                pools,
                localhost,
                localhost,
                this::nextPort,
                ledger,
                (kryo) -> kryo.register(MyMessage.class, 60), pool);

        cancellables.add(newNode);
        cancellables.add(ledger);

        if (!gossips.isEmpty()) {
            gossips.sort(
                    comparing(Gossip::nChannels)
                            .thenComparing(rndId::get));

            Gossip otherNode = gossips.get(0);
            newNode.join(otherNode.address());
        }

        gossips.add(newNode);
        gossips.forEach(node ->
                rndId.put(node, rnd.nextInt()));

        return newNode;
    }

    private <T extends Message> void listen(Gossip node, Class<T> myMessageClass, Consumer<T> listener) {
        cancellables.add(node.listen(false, myMessageClass, listener));
    }

    public int nextPort() {
        return port++;
    }

    public static class MyMessage extends Message {
        public MyMessage() {
            super();
        }
    }
}