package node.ledger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.factory.GossipFactory;
import node.Message;
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
        subActor = new MutableSet<>(factory.getActorTypes());
    }

    @Override
    public void copyFrom(UberActor obj) {
        if (obj == null) {
            subActor.clear();
        }
    }
}
