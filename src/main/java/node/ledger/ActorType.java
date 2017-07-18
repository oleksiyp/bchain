package node.ledger;

import node.GossipFactory;
import util.mutable.AbstractChoice;
import util.mutable.Mutable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ActorType<T extends Mutable<T>> extends AbstractChoice<T> {
    public ActorType(int tag,
                        String name,
                        Class<T> type,
                        Function<GossipFactory, T> constructor) {
        super(tag, name, type, (obj) -> constructor.apply((GossipFactory) obj));
    }

    public ActorType(int tag,
                        String name,
                        Class<T> type,
                        Supplier<T> constructor) {
        super(tag, name, type, (obj) -> constructor.get());
    }
}
