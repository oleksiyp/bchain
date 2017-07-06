package node.discovery2;

import node.Gossip;

public class AutoDiscovery2 {
    private Gossip gossip;

    public AutoDiscovery2(Gossip gossip) {
        this.gossip = gossip;
        gossip.send(new RequestNodeCountMessage());

        gossip.listen(false,
                RequestNodeCountMessage.class,
                this::receiveNodeCountMessage);
    }

    private void receiveNodeCountMessage(RequestNodeCountMessage msg) {
        gossip.send(new NodeCountMessage(1));
    }
}
