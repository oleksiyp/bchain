package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.GossipFactory;
import node.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class ReadEvent implements Mutable<ReadEvent> {
    private final Message message;

    public ReadEvent(GossipFactory factory) {
        message = factory.createMessage();
    }

    @Override
    public void copyFrom(ReadEvent obj) {
        if (obj == null) {
            message.clear();
            return;
        }
        message.copyFrom(obj.message);
    }
}
