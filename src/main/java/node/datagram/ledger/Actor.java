package node.datagram.ledger;

import node.datagram.Message;

import java.util.function.BiConsumer;

public interface Actor {
    NoBeheviour NO_BEHEVIOUR = new NoBeheviour();

    default BiConsumer<ActorContext, Message> initBehaviour() {
        return NO_BEHEVIOUR;
    }

    default BiConsumer<ActorContext, Message> duplicateBehaviour() {
        return NO_BEHEVIOUR;
    }

    default BiConsumer<ActorContext, Message> selfBehaviour() {
        return NO_BEHEVIOUR;
    }

    BiConsumer<ActorContext, Message> behaviour();

}

class NoBeheviour implements BiConsumer<ActorContext, Message> {
    @Override
    public void accept(ActorContext actorContext, Message message) {
    }
}
