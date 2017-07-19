package node.pong;

import node.counter.AckCountNodesMessage;
import node.counter.CountNodesActor;
import node.counter.CountNodesMessage;
import node.factory.GossipFactory;
import node.factory.Registry;

public class PongTypes {
    public static Registry<GossipFactory> messageRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, PongMessage.TYPE, PongMessage::new);

        return ret;
    }

    public static Registry<GossipFactory> actorRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, PongActor.TYPE, PongActor::new);

        return ret;
    }

}
