package node.ledger;

import node.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Cancelable;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class Ledger implements Cancelable {
    public static final Logger LOGGER = LogManager.getLogger(Ledger.class);

    private LedgerDispatcher dispatcher;
    private LedgerPartition []partitions;
    private LedgerListener ledgerListener;

    public Ledger(int retentionSize,
                  long retentionTime,
                  LedgerDispatcher dispatcher) {

        this.dispatcher = dispatcher;
        partitions = new LedgerPartition[dispatcher.getNPartitions()];
        for (int i = 0; i < partitions.length; i++) {
            partitions[i] = new LedgerPartition(retentionSize, retentionTime);
        }
    }

    public void setLedgerListener(LedgerListener ledgerListener) {
        this.ledgerListener = ledgerListener;
    }

    public void processMessage(Message message, String receiveChannelId) {
        dispatcher.dispatch((event) ->
                event.setLedgerListener(ledgerListener)
                        .setPartitions(partitions)
                        .processMessage()
                        .activate(message, receiveChannelId));
    }


    public CountDownLatch replayLedger(Consumer<Message> listener) {
        CountDownLatch latch = new CountDownLatch(1);
        dispatcher.dispatch((event) ->
                event.setLedgerListener(ledgerListener)
                        .setPartitions(partitions)
                        .replayLedger()
                        .activate(listener, latch));
        return latch;
    }

    @Override
    public void cancel() {
    }
}
