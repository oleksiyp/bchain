package node.ledger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import node.GossipNode;
import node.Message;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
@Slf4j
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
        log.debug("Activating {} at {}", self, gossipNode.address());
    }

    public <T> Message newMessage() {
        return gossipNode.getFactory().createMessage();
    }

    public void deactivateSelf() {
        log.debug("Deactivating {} at {}", self, gossipNode.address());
        uberSelf.getSubActor().deactivate(selfType);
    }

    public void stopGossip() {
        log.debug("Stopping gossipping a {}", message);
        message.getReceiver().copyFrom(gossipNode.address());
    }

    public void cancelMessage() {
        log.debug("Stopping processing a {}", message);
        message.clear();
    }
}