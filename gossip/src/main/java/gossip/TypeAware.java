package gossip;

import gossip.registry.RegistryItem;

public interface TypeAware {
    RegistryItem<?> getType();
}
