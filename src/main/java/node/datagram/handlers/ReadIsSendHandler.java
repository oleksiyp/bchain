package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.factory.GossipFactory;
import node.Message;
import node.datagram.event.Event;
import node.datagram.event.ReadEvent;
import node.datagram.event.SendEvent;

import static node.datagram.event.ReadEvent.READ_EVENT;

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
            SendEvent sendEvent = event.getSubEvent().activate(SendEvent.SEND_EVENT);
            sendEvent.getMessage().copyFrom(message);
        }
    }
}
