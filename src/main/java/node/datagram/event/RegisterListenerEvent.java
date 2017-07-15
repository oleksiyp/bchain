package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.Message;
import node.datagram.MessageType;
import util.mutable.Mutable;

import java.util.function.Consumer;

@Getter
@Setter
@ToString(exclude = "listener")
public class RegisterListenerEvent implements Mutable<RegisterListenerEvent> {
    private boolean add;
    private MessageType<?> messageType;
    private Consumer<Message> listener;

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
