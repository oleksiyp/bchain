package node.datagram;

import io.netty.util.collection.IntObjectHashMap;
import node.datagram.event.EventType;
import node.datagram.ledger.ActorType;
import node.datagram.ledger.UberActor;
import util.mutable.AbstractChoice;
import util.mutable.Choice;

import java.util.Map;

public class GossipFactoryImpl implements GossipFactory {
    private final Map<Integer, EventType<?>> eventTypeMap;
    private final Map<Integer, MessageType<?>> messageTypeMap;
    private final Map<Integer, HeaderType<?>> headerTypeMap;
    private final Map<Integer, ActorType<?>> actorTypeMap;

    public GossipFactoryImpl() {
        eventTypeMap = new IntObjectHashMap<>(EventType.SYSTEM_LEVEL.size());
        eventTypeMap.putAll(EventType.SYSTEM_LEVEL);

        messageTypeMap = new IntObjectHashMap<>(MessageType.SYSTEM_LEVEL.size());
        messageTypeMap.putAll(MessageType.SYSTEM_LEVEL);

        headerTypeMap = new IntObjectHashMap<>(MessageType.SYSTEM_LEVEL.size());
        headerTypeMap.putAll(HeaderType.SYSTEM_LEVEL);

        actorTypeMap = new IntObjectHashMap<>();
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
    public Map<Integer, EventType<?>> getEventTypes() {
        return eventTypeMap;
    }

    @Override
    public Map<Integer, MessageType<?>> getMessageTypes() {
        return messageTypeMap;
    }

    @Override
    public Map<Integer, HeaderType<?>> getHeaderTypes() {
        return headerTypeMap;
    }

    @Override
    public Map<Integer, ActorType<?>> getActorTypes() {
        return actorTypeMap;
    }

    public void register(EventType<?> eventType) {
        AbstractChoice.register(eventTypeMap, eventType);
    }

    public void register(MessageType<?> messageType) {
        AbstractChoice.register(messageTypeMap, messageType);
    }

    public void register(HeaderType<?> headerType) {
        AbstractChoice.register(headerTypeMap, headerType);
    }

    public void register(ActorType<?> actorType) {
        AbstractChoice.register(actorTypeMap, actorType);
    }
}
