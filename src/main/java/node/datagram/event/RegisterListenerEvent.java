package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.factory.GossipFactory;
import node.Message;
import node.MessageType;
import util.mutable.Mutable;

import java.util.function.Consumer;

@Getter
@Setter
@ToString(exclude = "listener")
public class RegisterListenerEvent implements Mutable<RegisterListenerEvent> {
    public static final EventType<RegisterListenerEvent> REGISTER_LISTENER_EVENT = new EventType<>(
            "REGISTER_LISTENER_EVENT",
            RegisterListenerEvent.class
    );
    private boolean add;
    private MessageType<?> messageType;
    private Consumer<Message> listener;

    public RegisterListenerEvent(GossipFactory factory) {

    }

    @Override
    public void copyFrom(RegisterListenerEvent obj) {
        if (obj == null) {
            add = false;
            messageType = null;
            listener = null;
            return;
        }
        add = obj.add;
        messageType = obj.messageType;
        listener = obj.listener;
    }
}
