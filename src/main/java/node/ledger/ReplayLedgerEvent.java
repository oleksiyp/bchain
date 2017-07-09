package node.ledger;

import node.Message;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ReplayLedgerEvent {
    private boolean active;
    private Consumer<Message> listener;
    private CountDownLatch latch;

    public void activate(Consumer<Message> listener, CountDownLatch latch) {
        this.active = true;
        this.listener = listener;
        this.latch = latch;
    }

    public boolean isActive() {
        return active;
    }

    public Consumer<Message> getListener() {
        return listener;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void clear() {
        this.active = false;
        this.listener = null;
        this.latch = null;
    }

    public void copyTo(ReplayLedgerEvent otherEvent) {
        otherEvent.active = active;
        otherEvent.listener = listener;
        otherEvent.latch = latch;
    }

    @Override
    public String toString() {
        return "ReplayLedgerEvent{" +
                "active=" + active +
                ", listener=" + listener +
                ", latch=" + latch +
                '}';
    }
}
