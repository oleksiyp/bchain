package node.datagram.ledger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.GossipFactory;
import node.datagram.Message;
import util.mutable.Mutable;
import util.mutable.MutableSet;

@Getter
@Setter
@ToString
public class UberActor implements Mutable<UberActor> {
    private final Message initialMessage;
    private final MutableSet<ActorType<?>> subActor;

    public UberActor(GossipFactory factory) {
        initialMessage = factory.createMessage();
        subActor = new MutableSet<>(factory.getActorTypes(), factory);
    }

    @Override
    public void copyFrom(UberActor obj) {
        if (obj == null) {
            subActor.clear();
        }
    }
}
