package kryo;

import org.objenesis.instantiator.ObjectInstantiator;
import util.Pool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class KryoObjectPool implements Pool {
    Map<Class<?>, PoolObjectInstantiator<?>> instantiatorMap;

    public KryoObjectPool() {
        instantiatorMap = new HashMap<>();
    }

    public <T> ObjectInstantiator<T> newInstantiator(Class<T> type, Supplier<T> factory, int maxPoolSize) {
        if (instantiatorMap.containsKey(type)) {
            return (ObjectInstantiator<T>) instantiatorMap.get(type);
        }
        PoolObjectInstantiator<T> instantiator;
        instantiator = new PoolObjectInstantiator<>(type,
                factory,
                maxPoolSize,
                this);
        instantiatorMap.put(type, instantiator);
        return instantiator;
    }

    @Override
    public void returnToPool(Object o) {
        if (!instantiatorMap.containsKey(o.getClass())) {
            throw new RuntimeException("No pool for " + o.getClass());
        }
        instantiatorMap.get(o.getClass())
                .returnToPool(o);
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        if (!instantiatorMap.containsKey(clazz)) {
            throw new RuntimeException("No pool for " + clazz);
        }
        return clazz.cast(instantiatorMap.get(clazz)
                .newInstance());
    }
}
