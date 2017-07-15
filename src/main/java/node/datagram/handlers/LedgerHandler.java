package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.Ledger;
import node.datagram.event.*;

import static node.datagram.event.EventType.READ_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

public class LedgerHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        Ledger ledger = event.getSelf().getGossipNode().getLedger();
        if (event.isSubEventActive(READ_EVENT)) {
            ReadEvent readEvent = event.getSubEvent(READ_EVENT);
            SendEvent sendEvent = event.getSubEvent(SEND_EVENT);
            sendEvent.copyFrom(readEvent);
            event.getSubEvent().activate(SEND_EVENT);
        }

        if (event.isSubEventActive(SEND_EVENT)) {
            onSendEvent(ledger, event.getSubEvent(SEND_EVENT));
        }
        if (sequence % 1000000 == 0) {
            System.out.println(sequence);
        }

    }

    private void onSendEvent(Ledger ledger, SendEvent sendEvent) {
        boolean inLedger = ledger.add(sendEvent.getMessage());
        sendEvent.setInLedger(inLedger);
    }
}
