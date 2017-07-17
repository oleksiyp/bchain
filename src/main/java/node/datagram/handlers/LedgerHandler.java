package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.ledger.Ledger;
import node.datagram.event.*;

import static node.datagram.event.EventType.READ_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

public class LedgerHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(SEND_EVENT)) {
            Ledger ledger = event.getSelf().getGossipNode().getLedger();
            SendEvent sendEvent = event.getSubEvent(SEND_EVENT);
            if (!onSendEvent(ledger, sendEvent)) {
                event.getSubEvent().clear();
            }
            if (sendEvent.getMessage().getSubType().activeChoice() == null) {
                event.getSubEvent().clear();
            }
        }
    }

    private boolean onSendEvent(Ledger ledger, SendEvent sendEvent) {
        return ledger.add(sendEvent.getMessage());
    }
}
