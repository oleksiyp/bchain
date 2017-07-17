package node.datagram.ledger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.GossipNode;
import node.datagram.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class ActorContext implements Mutable<ActorContext> {
    private long referenceId;
    private boolean initialization;
    private boolean addedToLedger;
    private UberActor uberSelf;
    private Actor self;
    private ActorType<?> selfType;
    private GossipNode gossipNode;
    private Message message;

    @Override
    public void copyFrom(ActorContext obj) {
        if (obj == null) {
            uberSelf = null;
            referenceId = 0;
            self = null;
            selfType = null;
            initialization = false;
            gossipNode = null;
            addedToLedger = false;
            message = null;
            return;
        }

        uberSelf = obj.uberSelf;
        referenceId = obj.referenceId;
        self = obj.self;
        selfType = obj.selfType;
        initialization = obj.initialization;
        gossipNode = obj.gossipNode;
        addedToLedger = obj.addedToLedger;
        message = obj.message;
    }

    public void activateSelf() {
        uberSelf.getSubActor().activate(selfType);
    }

    public <T> Message newMessage() {
        return gossipNode.getFactory().createMessage();
    }

    public void deactivateSelf() {
        uberSelf.getSubActor().deactivate(selfType);
    }

    public void stopGossip() {
        message.getReceiver().copyFrom(gossipNode.address());
    }

    public void cancelMessage() {
        message.clear();
    }
}
