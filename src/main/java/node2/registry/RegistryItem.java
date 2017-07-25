package node2.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import util.mutable.Mutable;

@Getter
@AllArgsConstructor
public abstract class RegistryItem<M> {
    private final String name;
    private final Class<M> type;

    @Override
    public String toString() {
        return name;
    }
}
