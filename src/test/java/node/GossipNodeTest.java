package node;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import node.discovery.AutoDiscovery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Cancelable;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.net.InetAddress.getByName;
import static java.util.Comparator.comparing;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class GossipNodeTest {
    public static final Logger LOGGER = LogManager.getLogger(GossipNodeTest.class);
    InetAddress localhost;
    MessageDigest md5;

    Random rnd;
    List<Gossip> gossips;
    List<Cancelable> cancellables;

    int port;
    GossipNode.Pools pools;
    ScheduledExecutorService scheduledExecutor;
    private Map<Gossip, Integer> rndId = new HashMap<>();
    private Executor executor;


    static class Runner {
        private Runnable runnable;

        public Runnable getRunnable() {
            return runnable;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }
    }

    long prevStart;
    long prevEnd;
    volatile long start;
    volatile long end;

    @Before
    public void setUp() throws Exception {
        port = 2000;

        rnd = new Random(555);
        gossips = new ArrayList<>();
        cancellables = new ArrayList<>();
        localhost = getByName("localhost");
        md5 = MessageDigest.getInstance("md5");

        pools = new GossipNode.Pools();
        cancellables.add(pools);

        DefaultThreadFactory factory = new DefaultThreadFactory("ledger");
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        cancellables.add(scheduledExecutor::shutdown);

        Disruptor<Runner> disruptor = new Disruptor<>(Runner::new, 1024 * 1024, factory);
        disruptor.handleEventsWith((EventHandler<Runner>) (event, sequence, endOfBatch) -> {
            end = sequence;
            event.getRunnable().run();
            event.setRunnable(null);
        });
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<Runner>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, Runner event) {
                LOGGER.error("Error processing " + sequence, ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                LOGGER.error("Error starting disruptor", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                LOGGER.error("Error shutting down disruptor", ex);
            }
        });
        ConcurrentLinkedQueue<Runnable> q = new ConcurrentLinkedQueue<>();
        RingBuffer<Runner> ringBuffer = disruptor.start();
        executor = (fromUser) -> {
            while (!q.isEmpty()) {
                Runnable fromQ = q.peek();
                if (!ringBuffer.hasAvailableCapacity(1)) {
                    q.add(fromUser);
                    return;
                }
                long n;
                try {
                    n = ringBuffer.tryNext();
                } catch (InsufficientCapacityException e) {
                    q.add(fromUser);
                    return;
                }
                try {
                    ringBuffer.get(n).setRunnable(fromQ);
                } finally {
                    ringBuffer.publish(n);
                    q.poll();
                }
            }

            if (!ringBuffer.hasAvailableCapacity(1)) {
                q.add(fromUser);
                return;
            }
            long n;
            try {
                n = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {
                q.add(fromUser);
                return;
            }

            start = n;
            try {
                ringBuffer.get(n).setRunnable(fromUser);
            } finally {
                ringBuffer.publish(n);
            }
        };
        cancellables.add(disruptor::shutdown);

        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);


        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
                long rbSize = start - end;

                System.out.println("Q: " + (q.size() + rbSize) +
                        " +" + (start - prevStart) +
                        " -" + (end - prevEnd));
                prevStart = start;
                prevEnd = end;
            }
        }).start();
    }

    @After
    public void tearDown() throws Exception {
        Collections.reverse(cancellables);
        cancellables.forEach(Cancelable::cancel);
    }

    @Test
    public void send100MessagesOn100Nodes() throws Exception {
        int nNodes = 100;
        int nMessages = 100;

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

        CompletableFuture<Void> doneFuture = new CompletableFuture<>();
        AtomicInteger done = new AtomicInteger();
        List<AutoDiscovery> autoDiscoveries = gossips.stream()
                .map(node ->
                        new AutoDiscovery(node,
                                rnd,
                                scheduledExecutor,
                                3,
                                10,
                                0,
                                10,
                                md5,
                                () -> {
                                    if (done.incrementAndGet() == gossips.size()) {
                                        doneFuture.complete(null);
                                    }
                                }))
                .peek(AutoDiscovery::start)
                .collect(Collectors.toList());

        try {
            doneFuture.get(500, SECONDS);
        } catch (TimeoutException ex) {
            System.out.println(
                    autoDiscoveries.stream()
                            .map(c -> c.getGossip().nChannels())
                            .collect(Collectors.toList()));
            throw ex;
        }

        out.println(gossips
                .stream()
                .map(Gossip::nChannels)
                .collect(Collectors.toList()));

        for (int i = 0; i < nMessages; i++) {
            gossips
                    .get(i % gossips.size())
                    .send(new MyMessage());
        }

        future.get(30, SECONDS);


    }

    private GossipNode createNode() {
        Ledger ledger = new Ledger(20000,
                HOURS.toMillis(100),
                executor);

        GossipNode newNode = new GossipNode(
                pools,
                localhost,
                localhost,
                this::nextPort,
                ledger,
                (kryo) -> kryo.register(MyMessage.class, 60));

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
        cancellables.add(node.listen(true, myMessageClass, listener));
    }

    public int nextPort() {
        return port++;
    }

    public static class MyMessage extends Message {
    }
}