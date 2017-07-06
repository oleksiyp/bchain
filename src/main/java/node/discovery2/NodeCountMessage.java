package node.discovery2;

import node.Message;

public class NodeCountMessage extends Message {
    private int count;

    public NodeCountMessage(int count) {
        this.count = count;
    }
}
