package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.event.Event;

public class ClearHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        event.clear();
    }
}
