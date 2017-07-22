package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.ledger.Ledger;
import node.datagram.event.*;

import static node.datagram.event.SendEvent.SEND_EVENT;

@Slf4j
public class LedgerHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(SEND_EVENT)) {
            Ledger ledger = event.getSelf().getGossipNode().getLedger();
            SendEvent sendEvent = event.getSubEvent(SEND_EVENT);
            if (!onSendEvent(ledger, sendEvent)) {
                log.trace("Message already in {} ledger. Clearing event", event.getSelf());
                event.getSubEvent().clear();
            }
            if (sendEvent.getMessage().getSubType().activeType() == null) {
                log.trace("{} message is cleared. Clearing event", event.getSelf());
                event.getSubEvent().clear();
            }
        }
    }

    private boolean onSendEvent(Ledger ledger, SendEvent sendEvent) {
        return ledger.add(sendEvent.getMessage());
    }
}
