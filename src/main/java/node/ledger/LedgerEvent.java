package node.ledger;

import util.Copyable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LedgerEvent implements Copyable<LedgerEvent> {
    private final ProcessMessageEvent processMessageEvent;
    private final ReplayLedgerEvent replayLedgerEvent;

    private LedgerPartition[] partitions;
    private LedgerListener ledgerListener;

    public LedgerEvent() {
        this.processMessageEvent = new ProcessMessageEvent();
        this.replayLedgerEvent = new ReplayLedgerEvent();
    }

    public ProcessMessageEvent processMessage() {
        return processMessageEvent;
    }

    public ReplayLedgerEvent replayLedger() {
        return replayLedgerEvent;
    }

    public void copyTo(LedgerEvent otherEvent) {
        processMessageEvent.copyTo(otherEvent.processMessageEvent);
        replayLedgerEvent.copyTo(otherEvent.replayLedgerEvent);
        otherEvent.ledgerListener = ledgerListener;
    }

    public void clear() {
        processMessageEvent.clear();
        replayLedgerEvent.clear();
        ledgerListener = null;
    }

    public LedgerListener getLedgerListener() {
        return ledgerListener;
    }

    public LedgerEvent setLedgerListener(LedgerListener ledgerListener) {
        this.ledgerListener = ledgerListener;
        return this;
    }

    @Override
    public String toString() {
        return "LedgerEvent{" +
                Stream.of(
                        (processMessageEvent.isActive()
                                ? "processMessageEvent=" + processMessageEvent
                                : null),

                        (replayLedgerEvent.isActive()
                                ? "replayLedgerEvent=" + replayLedgerEvent
                                : null))

                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", ")) +
                '}';
    }

    public LedgerPartition[] getPartitions() {
        return partitions;
    }

    public LedgerEvent setPartitions(LedgerPartition[] partitions) {
        this.partitions = partitions;
        return this;
    }
}
