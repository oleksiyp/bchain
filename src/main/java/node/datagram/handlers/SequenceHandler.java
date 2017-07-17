package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.datagram.event.Event;

@Slf4j
public class SequenceHandler implements EventHandler<Event> {
    private String type;
    private final EventHandler<Event>[] handlers;

    public SequenceHandler(String type, EventHandler<Event>... handlers) {
        this.type = type;
        this.handlers = handlers;
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        log.debug("{} {} {} Handling {}", event.getSelf(), type, sequence, event);
        for (int i = 0; i < handlers.length; i++) {
            log.trace("{} {} {} {} HANDLER", event.getSelf(), type, sequence, handlers[i].getClass().getSimpleName());
            handlers[i].onEvent(event, sequence, endOfBatch);
        }
        log.trace("{} {} {} Done.", event.getSelf(), type, sequence);
    }
}
