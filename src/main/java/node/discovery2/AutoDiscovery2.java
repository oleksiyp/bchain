package node.discovery2;

import node.Gossip;
import node.Headers;

public class AutoDiscovery2 {
    private Gossip gossip;

    public AutoDiscovery2(Gossip gossip) {
        this.gossip = gossip;

        gossip.listen(false,
                RequestNodeCountMessage.class,
                this::receiveRequestNodeCountMessage);

        gossip.routeBackListener(false,
                NodeCountMessage.class,
                this::receiveNodeCountMessage);
    }

    public void start() {
        gossip.send(new RequestNodeCountMessage());
    }

    private void receiveRequestNodeCountMessage(RequestNodeCountMessage msg) {
        NodeCountMessage countMessage = new NodeCountMessage(1);
        countMessage.getHeaders().set(Headers.ROUTE_BACK_ID, msg.getHeaders().getId());
        gossip.send(countMessage);
    }

    private void receiveNodeCountMessage(NodeCountMessage message) {
        System.out.println(message.getCount());
    }
}
