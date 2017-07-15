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
public class SendEvent implements Mutable<SendEvent> {
    private final Message message;
    private final Address senderAddress;
    private final Address receiverAddress;
    private boolean inLedger;

    public SendEvent() {
        message = new Message();
        senderAddress = new Address();
        receiverAddress = new Address();
    }

    @Override
    public void copyFrom(SendEvent obj) {
        if (obj == null) {
            inLedger = false;
            message.copyFrom(null);
            senderAddress.copyFrom(null);
            receiverAddress.copyFrom(null);
            return;
        }

        inLedger = obj.inLedger;
        message.copyFrom(obj.message);
        senderAddress.copyFrom(obj.senderAddress);
        receiverAddress.copyFrom(obj.receiverAddress);
    }

    @Override
    public void copyFrom(Object obj) {
        if (obj instanceof ReadEvent) {
            ReadEvent readEvent = (ReadEvent) obj;
            inLedger = readEvent.isInLedger();
            message.copyFrom(readEvent.getMessage());
            senderAddress.copyFrom(readEvent.getReceiveAddress());
            receiverAddress.copyFrom(null);
        } else if (obj instanceof SendEvent) {
            copyFrom((SendEvent) obj);
        } else {
            throw new UnsupportedOperationException("copyFrom");
        }
    }
}
