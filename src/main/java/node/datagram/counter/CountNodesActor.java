package node.datagram.counter;

import lombok.ToString;
import node.datagram.*;
import node.datagram.ledger.Actor;
import node.datagram.ledger.ActorContext;
import node.datagram.ledger.ActorType;
import util.mutable.Mutable;

import java.util.List;
import java.util.function.BiConsumer;

@ToString
public class CountNodesActor implements Actor, Mutable<CountNodesActor> {
    public static final ActorType<CountNodesActor> TYPE =
            new ActorType<>(
                    60,
                    "COUNT_NODES_ACTOR",
                    CountNodesActor.class,
                    CountNodesActor::new);

    private int parties;
    private int acks;
    private long count;
    private final Address sender;

    public CountNodesActor() {
        sender = new Address();
    }


    @Override
    public void copyFrom(CountNodesActor obj) {

    }

    private Message ackMessage(ActorContext ctx,
                               Address sender,
                               long count) {
        Message ackMessage = ctx.newMessage();

        ackMessage.activate(AckCountNodesMessage.TYPE)
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
            if (message.instanceOf(CountNodesMessage.TYPE)) {

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
            if (message.instanceOf(AckCountNodesMessage.TYPE)) {
                acks++;
                count += message.castTo(AckCountNodesMessage.TYPE).getCount();

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
            if (message.instanceOf(CountNodesMessage.TYPE)) {
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
