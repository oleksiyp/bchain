package node.factory;

import node.Address;
import node.HeaderType;
import node.Message;
import node.MessageType;
import node.datagram.event.EventType;
import node.ledger.ActorType;
import node.ledger.UberActor;
import util.mutable.MutableBlockingQueue;

public interface GossipFactory {
    Message createMessage();

    Address createAddress();

    UberActor createUberActor();

    MutableBlockingQueue<Message> createWriteQueue();

    RegistryMapping<EventType<?>>  getEventTypes();

    RegistryMapping<MessageType<?>> getMessageTypes();

    RegistryMapping<HeaderType<?>> getHeaderTypes();

    RegistryMapping<ActorType<?>> getActorTypes();

}
