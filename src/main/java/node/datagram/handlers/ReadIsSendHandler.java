package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.GossipFactory;
import node.datagram.Message;
import node.datagram.event.Event;
import node.datagram.event.ReadEvent;
import node.datagram.event.SendEvent;

import static node.datagram.event.EventType.READ_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

public class ReadIsSendHandler implements EventHandler<Event> {
    private Message message;

    public ReadIsSendHandler(GossipFactory factory) {
        message = new Message(factory);
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(READ_EVENT)) {
            System.out.println(event.getSelf().getAddress() + " " + event);
            ReadEvent readEvent = event.getSubEvent(READ_EVENT);
            message.copyFrom(readEvent.getMessage());
            SendEvent sendEvent = event.getSubEvent().activate(SEND_EVENT);
            sendEvent.getMessage().copyFrom(message);
        }
    }
}
