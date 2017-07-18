package node.pong;

import node.*;
import node.ledger.Actor;
import node.ledger.ActorContext;
import node.ledger.ActorType;
import util.Cancelable;
import util.mutable.Mutable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class PongActor implements Actor, Mutable<PongActor> {
    public static final ActorType<PongActor> TYPE =
            new ActorType<>(
                    61,
                    "PONG_ACTOR",
                    PongActor.class,
                    PongActor::new);


    private void replyPong(ActorContext ctx) {
        Message message = ctx.getMessage();
        if (!message.getSender().isSet()) {
            return;
        }

        GossipNode gossipNode = ctx.getGossipNode();

        Message ackMessage = ctx.newMessage();

        ackMessage.activate(PongMessage.TYPE);

        ackMessage
                .getHeaders()
                .activate(HeaderType.REFERENCE_ID)
                .setValue(ctx.getReferenceId());

        ackMessage
                .getReceiver()
                .copyFrom(message.getSender());

        gossipNode
                .send(ackMessage);
    }

    @Override
    public BiConsumer<ActorContext, Message> initBehaviour() {
        return (ctx, message) -> {
            if (message.instanceOf(MessageType.PING_MESSAGE_TYPE)) {
                replyPong(ctx);
            }
        };
    }

    @Override
    public BiConsumer<ActorContext, Message> behaviour() {
        return NO_BEHEVIOUR;
    }


    public static CompletableFuture<Address> join(Gossip gossip, Address address) {
        CompletableFuture<Address> future = new CompletableFuture<>();
        Cancelable cancelable = gossip.listen(PongMessage.TYPE, (message, pongMessage) -> {
            Address sender = new Address();
            sender.copyFrom(message.getSender());
            future.complete(sender);
        });
        gossip.join(address);
        future.thenRun(cancelable::cancel);
        return future;
    }

    @Override
    public void copyFrom(PongActor obj) {

    }
}
