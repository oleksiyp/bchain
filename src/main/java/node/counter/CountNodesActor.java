package node.counter;

import lombok.ToString;
import node.*;
import node.factory.GossipFactory;
import node.ledger.Actor;
import node.ledger.ActorContext;
import node.ledger.ActorType;
import util.mutable.Mutable;

import java.util.List;
import java.util.function.BiConsumer;

@ToString
public class CountNodesActor implements Actor, Mutable<CountNodesActor> {
    public static final ActorType<CountNodesActor> TYPE =
            new ActorType<>(
                    "COUNT_NODES_ACTOR",
                    CountNodesActor.class
            );

    private int parties;
    private int acks;
    private long count;
    private final Address sender;

    public CountNodesActor(GossipFactory factory) {
        sender = factory.createAddress();
    }


    @Override
    public void copyFrom(CountNodesActor obj) {

    }

    private Message ackMessage(ActorContext ctx,
                               Address sender,
                               long count) {
        Message ackMessage = ctx.newMessage();

        ackMessage.activate(AckCountNodesMessage.ACK_COUNT_NODES_MESSAGE)
                .setCount(count);

        ackMessage
                .getHeaders()
                .activate(HeaderType.REFERENCE_ID)
                .setValue(ctx.getReferenceId());

        ackMessage
                .getReceiver()
                .copyFrom(sender);

        return ackMessage;
    }

    public BiConsumer<ActorContext, Message> initBehaviour() {
        return (ctx, message) -> {
            GossipNode gossipNode = ctx.getGossipNode();
            if (message.instanceOf(CountNodesMessage.COUNT_NODES_MESSAGE_MESSAGE)) {

                RemoteParties remoteParties = gossipNode.getRemoteParties();
                List<Party> list = remoteParties.getList();
                parties = list.size();

                sender.copyFrom(message.getSender());

                if (sender.isSet()) {
                    parties--;
                }


                count = 1;
                ctx.activateSelf();

                if (parties == 0) {
                    reply(ctx);
                }
            }
        };
    }


    @Override
    public BiConsumer<ActorContext, Message> behaviour() {
        return (ctx, message) -> {
            if (message.instanceOf(AckCountNodesMessage.ACK_COUNT_NODES_MESSAGE)) {
                acks++;
                count += message.castTo(AckCountNodesMessage.ACK_COUNT_NODES_MESSAGE).getCount();

                if (parties == acks) {
                    reply(ctx);
                }
                ctx.cancelMessage();
            }
        };
    }

    private void reply(ActorContext ctx) {
        GossipNode gossipNode = ctx.getGossipNode();

        Message ackMessage = ackMessage(ctx,
                sender.isSet() ? sender : gossipNode.address(),
                count);

        gossipNode
                .send(ackMessage);

        ctx.deactivateSelf();
    }

    @Override
    public BiConsumer<ActorContext, Message> duplicateBehaviour() {
        return (ctx, message) -> {
            GossipNode gossipNode = ctx.getGossipNode();
            if (message.instanceOf(CountNodesMessage.COUNT_NODES_MESSAGE_MESSAGE)) {
                Message ackMessage = ackMessage(ctx,
                        message.getSender(),
                        0);

                gossipNode
                        .send(ackMessage);

                ackMessage.clear();
            }
        };
    }

}
