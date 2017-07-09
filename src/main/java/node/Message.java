package node;

import kryo.Poolable;
import util.Pool;

import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable, Poolable {
    public static final Random RANDOM = new Random();

    private Headers headers;

    public Message() {
        this.headers = new Headers();
        this.headers.setId(RANDOM.nextLong());
        this.headers.setTimestamp(System.currentTimeMillis());
    }

    public Headers getHeaders() {
        return headers;
    }

    @Override
    public void dispose(Pool pool) {
        headers.dispose(pool);
        headers = null;
        pool.returnToPool(this);
    }
}
