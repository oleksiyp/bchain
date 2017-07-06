package node;

import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable {
    public static final Random RANDOM = new Random();

    private final Headers headers;

    public Message() {
        this.headers = new Headers();
        this.headers.setId(RANDOM.nextLong());
        this.headers.setTimestamp(System.currentTimeMillis());
    }

    public Headers getHeaders() {
        return headers;
    }
}
