package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.Message;
import node.MessageType;
import node.datagram.Party;
import node.datagram.event.Event;
import node.datagram.event.RegisterListenerEvent;
import node.datagram.event.SendEvent;
import util.mutable.MutableUnion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static node.datagram.event.EventType.REGISTER_LISTENER_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

@Slf4j
public class MessageHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event,
                        long sequence,
                        boolean endOfBatch) throws Exception {
        Map<MessageType<?>, List<Consumer<Message>>> listeners = event.getSelf().getGossipNode().getListeners();

        if (event.isSubEventActive(SEND_EVENT)) {
            onSendEvent(event.getSelf(), event.getSubEvent(SEND_EVENT), listeners);

        } else if (event.isSubEventActive(REGISTER_LISTENER_EVENT)) {
            onRegisterListener(event.getSelf(), event.getSubEvent(REGISTER_LISTENER_EVENT), listeners);
        }
    }

    private void onSendEvent(Party self, SendEvent sendEvent, Map<MessageType<?>, List<Consumer<Message>>> listeners) {
        Message message = sendEvent.getMessage();
        MutableUnion<MessageType<?>> subType = message.getSubType();

        MessageType<?> messageType = subType.activeChoice();
        if (messageType == null) {
            log.trace("Empty message type in {}. Self={}", message, self);
            return;
        }

        List<Consumer<Message>> consumers = listeners.get(messageType);
        if (consumers == null) {
            log.trace("No listeners for {}. Self={}", messageType,  self);
            return;
        }

        log.trace("Notifying {} listeners with {}", consumers.size(), message);
        for (int i = 0; i < consumers.size(); i++) {
            consumers.get(i).accept(message);
        }
    }

    private void onRegisterListener(Party self, RegisterListenerEvent listenerEvent, Map<MessageType<?>, List<Consumer<Message>>> listeners) {
        MessageType<?> messageType = listenerEvent.getMessageType();
        Consumer<Message> listener = listenerEvent.getListener();

        if (listenerEvent.isAdd()) {
            log.trace("Adding to {} listener for {}", self, messageType);
            List<Consumer<Message>> consumers;
            consumers = listeners.computeIfAbsent(messageType,
                    (k) -> new ArrayList<>());
            consumers.add(listener);
        } else {
            log.trace("Removing to {} listener for {}", self, messageType);
            List<Consumer<Message>> consumers;
            consumers = listeners.computeIfAbsent(messageType,
                    (k) -> new ArrayList<>());
            consumers.remove(listener);
            if (consumers.isEmpty()) {
                listeners.remove(messageType);
            }
        }
    }

}
