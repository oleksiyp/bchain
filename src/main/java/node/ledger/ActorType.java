package node.ledger;

import util.mutable.ChoiceType;
import util.mutable.Mutable;

public class ActorType<T extends Mutable<T>> extends ChoiceType<T> {
    public ActorType(String name,
                     Class<T> type) {
        super(name, type);
    }
}
