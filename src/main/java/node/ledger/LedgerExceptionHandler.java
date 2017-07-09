package node.ledger;

import com.lmax.disruptor.ExceptionHandler;

class LedgerExceptionHandler implements ExceptionHandler<LedgerEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, LedgerEvent event) {
        Ledger.LOGGER.error("Error processing " + sequence, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        Ledger.LOGGER.error("Error starting disruptor", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        Ledger.LOGGER.error("Error shutting down disruptor", ex);
    }
}
