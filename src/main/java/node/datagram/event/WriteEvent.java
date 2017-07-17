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
    private Party party;

    public WriteEvent(GossipFactory factory) {
        message = factory.createMessage();
    }

    @Override
    public void copyFrom(WriteEvent obj) {
        if (obj == null) {
            party = null;
            message.clear();
            return;
        }

        party = obj.party;
        message.copyFrom(obj.message);
    }
}
