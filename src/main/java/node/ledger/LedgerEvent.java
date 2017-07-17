package node.ledger;

import util.mutable.Mutable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LedgerEvent implements Mutable<LedgerEvent> {
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

    @Override
    public void copyFrom(LedgerEvent otherEvent) {
        if (otherEvent == null) {
            processMessageEvent.clear();
            replayLedgerEvent.clear();
            ledgerListener = null;
        }
//        processMessageEvent.copyFromObj(otherEvent.processMessageEvent);
//        replayLedgerEvent.copyFromObj(otherEvent.replayLedgerEvent);
        ledgerListener = otherEvent.ledgerListener;
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
