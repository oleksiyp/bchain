package node.discovery2;

import node.Message;

public class NodeCountMessage extends Message {
    private final int count;

    public NodeCountMessage(int count) {
        super();
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
