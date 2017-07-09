package kryo;

import org.objenesis.instantiator.ObjectInstantiator;
import util.Pool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

class PoolObjectInstantiator<T> implements ObjectInstantiator<T> {
    private Queue<T> pool;
    private Class<T> clazz;
    private Supplier<T> factory;
    private final int maxPoolSize;
    private Pool generalPool;

    PoolObjectInstantiator(Class<T> clazz,
                           Supplier<T> factory,
                           int maxPoolSize,
                           Pool generalPool) {
        this.clazz = clazz;
        this.factory = factory;
        this.maxPoolSize = maxPoolSize;
        this.generalPool = generalPool;
        pool = new ConcurrentLinkedQueue<>();
    }

    @Override
    public T newInstance() {
        T poolObj = pool.poll();
        if (poolObj != null) {
            return poolObj;
        }

        return factory.get();
    }

    public void returnToPool(Object newObj) {

        if (clazz.isAssignableFrom(newObj.getClass())) {
            if (pool.size() > maxPoolSize) {
                return;
            }
            pool.add(clazz.cast(newObj));
        } else {
            generalPool.returnToPool(newObj);
        }
    }
}
