package node.factory;

import node.HeaderType;
import node.datagram.event.*;
import node.pong.PingMessage;
import util.mutable.MutableLong;

public class GossipTypes {

    public static Registry<GossipFactory> eventRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, ReadEvent.READ_EVENT, ReadEvent::new);
        ret.register(1, WriteEvent.TYPE, WriteEvent::new);
        ret.register(2, SendEvent.SEND_EVENT, SendEvent::new);
        ret.register(3, RegisterListenerEvent.REGISTER_LISTENER_EVENT, RegisterListenerEvent::new);
        ret.register(4, RegisterPartyEvent.REGISTER_PARTY_EVENT, RegisterPartyEvent::new);

        return ret;
    }

    public static Registry<GossipFactory> messageRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, PingMessage.TYPE, PingMessage::new);

        return ret;
    }


    public static Registry<GossipFactory> headersRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        ret.register(0, HeaderType.REFERENCE_ID, MutableLong::new);

        return ret;
    }

    public static Registry<GossipFactory> actorRegistry() {
        Registry<GossipFactory> ret = new Registry<>();

        return ret;
    }
}
