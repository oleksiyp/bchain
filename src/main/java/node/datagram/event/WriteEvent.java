package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.GossipFactory;
import node.datagram.Message;
import node.datagram.Party;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class WriteEvent implements Mutable<WriteEvent> {
    private final Message message;
    private Party to;

    public WriteEvent(GossipFactory factory) {
        message = factory.createMessage();
    }

    @Override
    public void copyFrom(WriteEvent obj) {
        if (obj == null) {
            to = null;
            message.clear();
            return;
        }

        to = obj.to;
        message.copyFrom(obj.message);
    }
}
