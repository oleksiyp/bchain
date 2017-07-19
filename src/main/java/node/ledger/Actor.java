package node.ledger;

import node.Message;

import java.util.function.BiConsumer;

public interface Actor {
    BiConsumer<ActorContext, Message> NO_BEHAVIOUR = (ctx, msg) -> {};

    default BiConsumer<ActorContext, Message> initBehaviour() {
        return NO_BEHAVIOUR;
    }

    default BiConsumer<ActorContext, Message> duplicateBehaviour() {
        return NO_BEHAVIOUR;
    }

    default BiConsumer<ActorContext, Message> selfBehaviour() {
        return NO_BEHAVIOUR;
    }

    BiConsumer<ActorContext, Message> behaviour();

}

