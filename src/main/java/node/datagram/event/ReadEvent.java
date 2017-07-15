package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.Address;
import node.datagram.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class ReadEvent implements Mutable<ReadEvent> {
    private final Message message;
    private final Address receiveAddress;
    private boolean inLedger;

    public ReadEvent() {
        message = new Message();
        receiveAddress = new Address();
    }


    @Override
    public void copyFrom(ReadEvent obj) {
        if (obj == null) {
            message.copyFrom(null);
            receiveAddress.copyFrom(null);
            inLedger = false;
            return;
        }
        message.copyFrom(obj.message);
        receiveAddress.copyFrom(obj.receiveAddress);
        inLedger = obj.inLedger;
    }
}
