package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.factory.GossipFactory;
import node.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class ReadEvent implements Mutable<ReadEvent> {
    public static final EventType<ReadEvent> READ_EVENT = new EventType<>(
            "READ_EVENT",
            ReadEvent.class);

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
