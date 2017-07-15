package node.datagram;

import util.MappedQueue;

public class Ledger {
    MappedQueue<Message> mappedQueue;

    public Ledger(MappedQueue<Message> mappedQueue) {
        this.mappedQueue = mappedQueue;
    }

    public boolean add(Message message) {
        return false;
    }
}
