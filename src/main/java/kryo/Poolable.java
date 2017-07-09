package kryo;

import util.Pool;

public interface Poolable {
    void dispose(Pool pool);
}
