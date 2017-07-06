package node;

import io.netty.channel.ChannelId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Cancelable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.lang.Math.abs;

public class Ledger implements Cancelable {
    public static final Logger LOGGER = LogManager.getLogger(Ledger.class);

    private LedgerListener listener;

    private LinkedList<Message> ledger;
    private Map<Long, Message> ledgerIndex;
    private Map<Long, ChannelId> channelIndex;

    private final Executor executor;
    private int retentionSize;
    private long retentionTime;

    public Ledger(int retentionSize, long retentionTime, Executor executor) {
        this.executor = executor;
        ledger = new LinkedList<>();
        ledgerIndex = new HashMap<>();
        channelIndex = new HashMap<>();
        this.retentionSize = retentionSize;
        this.retentionTime = retentionTime;
    }

    public void setListener(LedgerListener listener) {
        this.listener = listener;
    }

    public void sendMessage(Message message, ChannelId receiveChannelId) {
        exec(() -> {
            if (processMessage(message, receiveChannelId)) {
                listener.notifyListeners(message);
                listener.broadcastChannels(message, receiveChannelId);
            }
        });
    }


    public CountDownLatch replayLedger(Consumer<Message> listener) {
        CountDownLatch latch = new CountDownLatch(1);
        exec(() -> {
            ledger.forEach(listener);
            latch.countDown();
        });
        return latch;
    }

    public void sendViaBackRoute(Message message) {
        exec(() -> {
            listener.notifyListeners(message);

            Long key = message.getHeaders().get(Headers.ROUTE_BACK_ID);
            if (key == null) {
                return;
            }
            ChannelId channel = channelIndex.get(key);
            if (channel == null) {
                return;
            }
            listener.sendChannel(channel, message);

        });
    }

    private void exec(Runnable runnable) {
        executor.execute(runnable);
    }

    private boolean processMessage(Message message, ChannelId receiveChannel) {
        long id = message.getHeaders().getId();

        if (ledgerIndex.containsKey(id)) {
            return false;
        }

        long time = System.currentTimeMillis();

        if (abs(message.getHeaders().getTimestamp() - time) > retentionTime) {
            return false;
        }

        ledger.add(message);
        ledgerIndex.put(id, message);
        if (receiveChannel != null) {
            channelIndex.put(id, receiveChannel);
        }

        cleanupLedger(time);
        return true;
    }

    private void cleanupLedger(long time) {
        while (!ledger.isEmpty()) {
            Message first = ledger.getFirst();
            if (time - first.getHeaders().getTimestamp() < retentionTime &&
                    ledger.size() <= retentionSize) {
                break;
            }
            ledger.removeFirst();
            long id = first.getHeaders().getId();
            ledgerIndex.remove(id);
            channelIndex.remove(id);
        }
    }

    @Override
    public void cancel() {
    }

}
