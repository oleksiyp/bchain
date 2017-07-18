package node.datagram.ledger;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import node.datagram.GossipNode;
import node.datagram.HeaderType;
import node.datagram.Message;
import util.MappedQueue;
import util.mutable.Mutable;

import java.util.function.Consumer;

@Getter
@Setter
@Slf4j
public class Ledger {
    private final MappedQueue<UberActor> mappedQueue;
    private final ActorContext actorContext;
    private GossipNode gossipNode;

    public Ledger(MappedQueue<UberActor> mappedQueue,
                  ActorContext actorContext) {
        this.mappedQueue = mappedQueue;
        this.actorContext = actorContext;
    }

    public boolean add(Message message) {
        boolean addedToLedger = mappedQueue.add(message.getId(), message.getTimestamp());

        log.debug("Added {} to {} ledger. {}",
                message,
                gossipNode.address(),
                addedToLedger ? "New message" : "Already in ledger");

        act(message, addedToLedger);
        return addedToLedger;
    }

    private void act(Message message, boolean addedToLedger) {
        long refId;
        boolean init;
        if (message.hasHeader(HeaderType.REFERENCE_ID)) {
            refId = message.getHeader(HeaderType.REFERENCE_ID).getValue();
            init = false;
        } else {
            refId = message.getId();
            init = true;
        }

        UberActor uberActor = mappedQueue.get(refId);
        if (uberActor == null) {
            return;
        }

        if (init) {
            uberActor
                    .getInitialMessage()
                    .copyFrom(message);
        }

        actorContext.setGossipNode(gossipNode);
        actorContext.setUberSelf(uberActor);
        actorContext.setReferenceId(refId);
        actorContext.setAddedToLedger(addedToLedger);
        actorContext.setInitialization(init);
        actorContext.setMessage(message);

        Consumer<ActorType<?>> consumer = type -> {
            Mutable<?> mutable = uberActor.getSubActor().get(type);
            if (mutable instanceof Actor) {
                Actor actor = (Actor) mutable;

                actorContext.setSelf(actor);
                actorContext.setSelfType(type);

                String msgText = message.toString();

                if (!addedToLedger) {
                    actor.duplicateBehaviour()
                            .accept(actorContext, message);
                } else if (init) {
                    actor.initBehaviour()
                            .accept(actorContext, message);
                } else if (!message.getSender().isSet()){
                    actor.selfBehaviour()
                            .accept(actorContext, message);
                } else {
                    actor.behaviour()
                            .accept(actorContext, message);
                }


                boolean activated = uberActor.getSubActor().isActive(type);
                if (!(addedToLedger && init && !activated)) {
                    log.info("Dispatched {} to {} at {}: {}{}", msgText, actor, gossipNode.address(),
                            addedToLedger ? "New to Ledger. " : "Already in Ledger. ",
                            init ? (activated ? "Initialized actor. " : "") : "Referenced by ID. ",
                            message.getSender().isSet() ? "Received message. " : "Self sent message. ");
                }
            }
        };

        if (init) {
            uberActor.getSubActor().iterateAll(consumer);
        } else {
            uberActor.getSubActor().iterateActive(consumer);
        }

        actorContext.clear();
    }
}
