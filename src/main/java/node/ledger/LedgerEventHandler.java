package node.ledger;

import com.lmax.disruptor.EventHandler;
import node.Headers;
import node.Message;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.util.stream.Stream.of;

class LedgerEventHandler implements EventHandler<LedgerEvent> {
    private final int handlerId;
    private final long nPartitions;

    private LedgerListener ledgerListener;
    private LedgerPartition[] partitions;
    private long sequence;
    private int currentPartition;

    public LedgerEventHandler(int handlerId,
                              long nPartitions) {
        this.handlerId = handlerId;
        this.nPartitions = nPartitions;
    }


    @Override
    public void onEvent(LedgerEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            ledgerListener = event.getLedgerListener();
            this.partitions = event.getPartitions();
            this.sequence = sequence;
            currentPartition = (int) (sequence % nPartitions);

            ProcessMessageEvent processMessageEvent = event.processMessage();

            if (processMessageEvent.isActive()) {

                Message message = processMessageEvent.getMessage();
                Headers headers = message.getHeaders();

                if (headers.isRouteBack()) {
                    handleRouteBack(event);
                } else if (onlyForThisPartition()) {
                    handleProcessMessage(processMessageEvent);
                }
            }

            handleReplayLedger(event.replayLedger());

            if (endOfBatch) {
                cleanupLedger();
            }
        } finally {
            ledgerListener = null;
            partitions = null;
            this.sequence = 0;
            this.currentPartition = 0;
        }
    }

    private void handleRouteBack(LedgerEvent event) {
        Message message = event.processMessage().getMessage();
        Headers headers = message.getHeaders();

        Long id = headers.get(Headers.ROUTE_BACK_ID);
        if (id == null) {
            return;
        }

        LedgerActor actor = currentPartition()
                .actorById(id);

        if (actor != null) {
            actor.accept(event.processMessage());
        }
    }

    private void handleProcessMessage(ProcessMessageEvent processMessageEvent) {
        if (!processMessageEvent.isActive()) {
            return;
        }

        Message message = processMessageEvent.getMessage();
        Headers headers = message.getHeaders();

        long id = headers.getId();
        long timestamp = headers.getTimestamp();

        LedgerActor actor = currentPartition()
                .addActor(id, timestamp);
        if (actor != null) {
            actor.setLedgerListener(ledgerListener);
            actor.accept(processMessageEvent);
        }
    }

    private void handleReplayLedger(ReplayLedgerEvent replayLedgerEvent) {
        if (!replayLedgerEvent.isActive()) {
            return;
        }

        CountDownLatch latch = replayLedgerEvent.getLatch();
        LedgerPartition partition = currentPartition();
        try {
            for (LedgerActor actor : partition) {
                actor.accept(replayLedgerEvent);
            }
        } finally {
            latch.countDown();
        }
    }

    private LedgerPartition currentPartition() {
        return partitions[currentPartition];
    }

    private boolean onlyForThisPartition() {
        return currentPartition == handlerId;
    }

    private void cleanupLedger() {
        currentPartition().cleanup();
    }

}
