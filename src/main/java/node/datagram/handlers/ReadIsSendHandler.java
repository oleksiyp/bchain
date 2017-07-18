package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.GossipFactory;
import node.Message;
import node.datagram.event.Event;
import node.datagram.event.ReadEvent;
import node.datagram.event.SendEvent;

import static node.datagram.event.EventType.READ_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

@Slf4j
public class ReadIsSendHandler implements EventHandler<Event> {
    private Message message;

    public ReadIsSendHandler(GossipFactory factory) {
        message = new Message(factory);
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(READ_EVENT)) {
            ReadEvent readEvent = event.getSubEvent(READ_EVENT);
            message.copyFrom(readEvent.getMessage());
            log.trace("Converting read to send {}", message);
            SendEvent sendEvent = event.getSubEvent().activate(SEND_EVENT);
            sendEvent.getMessage().copyFrom(message);
        }
    }
}
