package node;

import node.datagram.event.EventType;
import node.ledger.ActorType;
import node.ledger.UberActor;

import java.util.Map;

public interface GossipFactory {
    Message createMessage();

    Address createAddress();

    UberActor createUberActor();

    Map<Integer, EventType<?>> getEventTypes();

    Map<Integer, MessageType<?>> getMessageTypes();

    Map<Integer, HeaderType<?>> getHeaderTypes();

    Map<Integer, ActorType<?>> getActorTypes();

}
