package node.discovery;

import node.Gossip;
import node.Message;
import node.discovery.IntroduceMessage;
import node.discovery.JoinRequestMessage;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static node.Headers.ROUTE_BACK_ID;
import static node.Headers.ROUTE_BACK_TARGET;

public class AutoDiscovery {
    public static final Charset UTF_8 = Charset.forName("UTF8");

    private Gossip gossip;
    private Random rnd;
    private ScheduledExecutorService executor;
    private int bitsHash;
    private int channelsLimit;
    private int delay;
    private int delayInterval;
    private MessageDigest messageDigest;
    private final long thisHash;
    private Runnable doneHandler;

    public AutoDiscovery(Gossip gossip,
                         Random rnd,
                         ScheduledExecutorService executor,
                         int bitsHash,
                         int channelsLimit,
                         int delay,
                         int delayInterval,
                         MessageDigest messageDigest,
                         Runnable doneHandler) {
        this.gossip = gossip;
        this.rnd = rnd;
        this.executor = executor;
        this.bitsHash = bitsHash;
        this.channelsLimit = channelsLimit;
        this.delay = delay;
        this.delayInterval = delayInterval;
        this.messageDigest = messageDigest;
        thisHash = hash(gossip.address());
        this.doneHandler = doneHandler;
    }

    private long hash(InetSocketAddress address) {
        try {
            byte[] result = messageDigest.digest(address.toString().getBytes(UTF_8));
            long ret = 0;
            for (int i = 0; i < result.length; i++) {
                long r = result[i];
                r += 256;
                r &= 255;
                r <<= (i * 8) % 128;
                ret = ret | r;
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static long bits(int n, Random rnd) {
        long ret = 0;
        for (int bitsOn = 0; bitsOn < n;) {
            int bit = rnd.nextInt(64);
            long mask = 1L << bit;
            if ((ret & mask) > 0) {
                continue;
            }

            ret |= mask;
            bitsOn++;
        }
        return ret;
    }

    public void start() {
        gossip.listen(false,
                IntroduceMessage.class,
                this::receive);

        gossip.routeBackListener(false,
                JoinRequestMessage.class,
                this::receiveJoinRequest);

        gossip.listenMembership(this::joined, null);

        for (int i = 0; i < bitsHash; i++) {

            int ii = i;
            executor.schedule(() -> send(bitsHash - ii),
                    i * delayInterval + delay,
                    TimeUnit.SECONDS);
        }
    }

    private void joined(InetSocketAddress address) {
        if (checkDone()) {
            synchronized (this) {
                if (doneHandler != null) {
                    doneHandler.run();
                    doneHandler = null;
                }
            }
        }
    }

    private void send(int nBits) {
        if (checkDone()) return;

        long mask = bits(nBits, rnd);
        gossip.send(new IntroduceMessage(thisHash, mask));
    }

    private boolean checkDone() {
        return gossip.nChannels() >= channelsLimit;
    }


    private void receive(IntroduceMessage message) {
        InetSocketAddress originator = message.getHeaders().getOriginator();

        if (gossip.address().equals(originator)) {
            return;
        }

        long mask = message.getMask();
        long thatHash = message.getHash();

        if ((thatHash & mask) != (thisHash & mask)) {
            return;
        }

        Message backMessage = new JoinRequestMessage(gossip.address());
        backMessage.getHeaders().set(ROUTE_BACK_ID, message.getHeaders().getId());
        backMessage.getHeaders().set(ROUTE_BACK_TARGET, message.getHeaders().getOriginator());
        gossip.routeBack(backMessage);
    }


    private void receiveJoinRequest(JoinRequestMessage message) {
        if (checkDone()) return;

        gossip.join(message.getAddress());
    }

    public Gossip getGossip() {
        return gossip;
    }
}
