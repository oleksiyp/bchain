package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.GossipFactory;
import node.datagram.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class SendEvent implements Mutable<SendEvent> {
    private final Message message;

    public SendEvent(GossipFactory factory) {
        message = factory.createMessage();
    }

    @Override
    public void copyFrom(SendEvent obj) {
        if (obj == null) {
            message.clear();
            return;
        }

        message.copyFrom(obj.message);
    }

    @Override
    public void copyFromObj(Object obj) {
        if (obj instanceof ReadEvent) {
            ReadEvent readEvent = (ReadEvent) obj;
            message.copyFrom(readEvent.getMessage());
        } else if (obj instanceof SendEvent) {
            copyFrom((SendEvent) obj);
        } else {
            throw new UnsupportedOperationException("copyFromObj");
        }
    }
}
