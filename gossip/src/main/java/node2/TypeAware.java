package node2;

import node2.registry.RegistryItem;

public interface TypeAware {
    RegistryItem<?> getType();
}
