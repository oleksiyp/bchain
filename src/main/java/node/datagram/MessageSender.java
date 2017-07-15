package node.datagram;

import java.util.function.Consumer;

public interface MessageSender {
    void write(int n, Consumer<Message> consumer);
}
