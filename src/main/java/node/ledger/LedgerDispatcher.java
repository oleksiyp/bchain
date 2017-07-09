package node.ledger;

import com.lmax.disruptor.EventHandler;
import util.Dispatcher;
import util.Pool;

import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.of;

public class LedgerDispatcher extends Dispatcher<LedgerEvent> {
    private int nPartitions;
    private final LedgerEventHandler[] handlers;

    public LedgerDispatcher(int ringBufferSize,
                            ThreadFactory threadFactory,
                            int nPartitions) {

        super(LedgerEvent::new, ringBufferSize, threadFactory);

        handlers = range(0, nPartitions)
                .mapToObj(handlerId ->
                        new LedgerEventHandler(
                                handlerId,
                                nPartitions))
                .collect(Collectors.toList())
                .toArray(new LedgerEventHandler[0]);

        disruptor.handleEventsWith(handlers)
                .then(new DisposeHandler());

        disruptor.setDefaultExceptionHandler(new LedgerExceptionHandler());

        this.nPartitions = nPartitions;
    }

    public int getNPartitions() {
        return nPartitions;
    }

    public long getStart() {
        return disruptor.getCursor();
    }

    public long getEnd() {
        return of(handlers)
                .mapToLong(disruptor::getSequenceValueFor)
                .max()
                .orElse(0);
    }

    public long getQSize() {
        return queue.size();
    }

    private static class DisposeHandler implements EventHandler<LedgerEvent> {
        @Override
        public void onEvent(LedgerEvent event, long sequence, boolean endOfBatch) throws Exception {
            ProcessMessageEvent processMessage = event.processMessage();
            Pool pool = event.getLedgerListener().getPool();
            if (processMessage.isActive()) {
                processMessage.getMessage().dispose(pool);
            }
            event.clear();
        }
    }
}
