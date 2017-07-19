package node.factory;

import node.Address;
import node.HeaderType;
import node.Message;
import node.MessageType;
import node.datagram.event.EventType;
import node.ledger.ActorType;
import node.ledger.UberActor;

public interface GossipFactory {
    Message createMessage();

    Address createAddress();

    UberActor createUberActor();

    RegistryMapping<EventType<?>>  getEventTypes();

    RegistryMapping<MessageType<?>> getMessageTypes();

    RegistryMapping<HeaderType<?>> getHeaderTypes();

    RegistryMapping<ActorType<?>> getActorTypes();

}
