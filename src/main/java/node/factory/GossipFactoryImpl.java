package node.factory;

import node.Address;
import node.HeaderType;
import node.Message;
import node.MessageType;
import node.datagram.event.Event;
import node.datagram.event.EventType;
import node.ledger.ActorType;
import node.ledger.UberActor;

public class GossipFactoryImpl implements GossipFactory {
    private final Registry<GossipFactory> eventTypes;
    private final Registry<GossipFactory> messageTypes;
    private final Registry<GossipFactory> headerTypes;
    private final Registry<GossipFactory> actorTypes;

    public GossipFactoryImpl(
            Registry<GossipFactory> eventTypes,
            Registry<GossipFactory> messageTypes,
            Registry<GossipFactory> headerTypes,
            Registry<GossipFactory> actorTypes) {

        this.eventTypes = eventTypes;
        this.messageTypes = messageTypes;
        this.headerTypes = headerTypes;
        this.actorTypes = actorTypes;
    }

    @Override
    public Message createMessage() {
        return new Message(this);
    }

    @Override
    public Address createAddress() {
        return new Address();
    }

    @Override
    public UberActor createUberActor() {
        return new UberActor(this);
    }

    @Override
    public RegistryMapping<EventType<?>> getEventTypes() {
        eventTypes.setConstructorParam(this);
        return eventTypes.mapping(EventType.class);
    }

    @Override
    public RegistryMapping<MessageType<?>> getMessageTypes() {
        messageTypes.setConstructorParam(this);
        return messageTypes.mapping(MessageType.class);
    }

    @Override
    public RegistryMapping<HeaderType<?>> getHeaderTypes() {
        headerTypes.setConstructorParam(this);
        return headerTypes.mapping(HeaderType.class);
    }

    @Override
    public RegistryMapping<ActorType<?>> getActorTypes() {
        actorTypes.setConstructorParam(this);
        return actorTypes.mapping(ActorType.class);
    }
}
