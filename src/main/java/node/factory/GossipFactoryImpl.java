package node.factory;

import node.Address;
import node.HeaderType;
import node.Message;
import node.MessageType;
import node.datagram.event.EventType;
import node.ledger.ActorType;
import node.ledger.UberActor;
import util.mutable.MutableBlockingQueue;

public class GossipFactoryImpl implements GossipFactory {
    private final Registry<GossipFactory> eventTypes;
    private final Registry<GossipFactory> messageTypes;
    private final Registry<GossipFactory> headerTypes;
    private final Registry<GossipFactory> actorTypes;
    private int writeMsgsBuf;

    public GossipFactoryImpl(
            Registry<GossipFactory> eventTypes,
            Registry<GossipFactory> messageTypes,
            Registry<GossipFactory> headerTypes,
            Registry<GossipFactory> actorTypes, int writeMsgsBuf) {

        this.eventTypes = eventTypes;
        this.messageTypes = messageTypes;
        this.headerTypes = headerTypes;
        this.actorTypes = actorTypes;
        this.writeMsgsBuf = writeMsgsBuf;
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
    public MutableBlockingQueue<Message> createWriteQueue() {
        return new MutableBlockingQueue<>(this::createMessage, writeMsgsBuf);
    }

    @Override
    public RegistryMapping<EventType<?>> getEventTypes() {
        return eventTypes.mapping(this, EventType.class);
    }

    @Override
    public RegistryMapping<MessageType<?>> getMessageTypes() {
        return messageTypes.mapping(this, MessageType.class);
    }

    @Override
    public RegistryMapping<HeaderType<?>> getHeaderTypes() {
        return headerTypes.mapping(this, HeaderType.class);
    }

    @Override
    public RegistryMapping<ActorType<?>> getActorTypes() {
        return actorTypes.mapping(this, ActorType.class);
    }
}
