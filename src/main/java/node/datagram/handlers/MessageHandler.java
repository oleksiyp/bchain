package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.Message;
import node.datagram.MessageType;
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

public class MessageHandler implements EventHandler<Event> {
    @Override
    public void onEvent(Event event,
                        long sequence,
                        boolean endOfBatch) throws Exception {
        Map<MessageType<?>, List<Consumer<Message>>> listeners = event.getSelf().getGossipNode().getListeners();

        if (event.isSubEventActive(SEND_EVENT)) {
            onSendEvent(event.getSubEvent(SEND_EVENT), listeners);

        } else if (event.isSubEventActive(REGISTER_LISTENER_EVENT)) {
            onRegisterListener(event.getSubEvent(REGISTER_LISTENER_EVENT), listeners);
        }
    }

    private void onSendEvent(SendEvent sendEvent, Map<MessageType<?>, List<Consumer<Message>>> listeners) {
        Message message = sendEvent.getMessage();
        MutableUnion<MessageType<?>> subType = message.getSubType();

        MessageType<?> messageType = subType.activeChoice();
        if (messageType == null) {
            return;
        }

        List<Consumer<Message>> consumers = listeners.get(messageType);
        if (consumers == null) {
            return;
        }

        for (int i = 0; i < consumers.size(); i++) {
            consumers.get(i).accept(message);
        }
    }

    private void onRegisterListener(RegisterListenerEvent listenerEvent, Map<MessageType<?>, List<Consumer<Message>>> listeners) {
        MessageType<?> messageType = listenerEvent.getMessageType();
        Consumer<Message> listener = listenerEvent.getListener();

        if (listenerEvent.isAdd()) {
            List<Consumer<Message>> consumers;
            consumers = listeners.computeIfAbsent(messageType,
                    (k) -> new ArrayList<>());
            consumers.add(listener);
        } else {
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
