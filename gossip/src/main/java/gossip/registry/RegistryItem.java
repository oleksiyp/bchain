package gossip.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
