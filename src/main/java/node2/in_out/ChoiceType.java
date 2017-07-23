package node2.in_out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import util.mutable.Mutable;

@Getter
@AllArgsConstructor
public abstract class ChoiceType<M> {
    private final String name;
    private final Class<M> type;

    @Override
    public String toString() {
        return name;
    }
}
