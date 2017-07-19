package node.counter;

import node.factory.GossipFactory;
import node.factory.Registry;

import static node.counter.AckCountNodesMessage.ACK_COUNT_NODES_MESSAGE;
import static node.counter.CountNodesMessage.COUNT_NODES_MESSAGE_MESSAGE;

public class CountNodesTypes {
    public static Registry<GossipFactory> messageRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, COUNT_NODES_MESSAGE_MESSAGE, CountNodesMessage::new);
        ret.register(1, ACK_COUNT_NODES_MESSAGE, AckCountNodesMessage::new);

        return ret;
    }

    public static Registry<GossipFactory> actorRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, CountNodesActor.TYPE, CountNodesActor::new);

        return ret;
    }

}
