package node.datagram;

import lombok.Getter;
import lombok.Setter;
import util.Serializable;
import util.mutable.Mutable;
import util.mutable.MutableSet;
import util.mutable.MutableUnion;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.function.Consumer;

@Getter
@Setter
public class Message implements Mutable<Message>, Serializable {
    private long id;
    private long timestamp;
    private final Address sender;
    private final Address receiver;
    private final Address origin;

    private final MutableUnion<MessageType<?>> subType;
    private final MutableSet<HeaderType<?>> headers;

    public Message(GossipFactory factory) {
        this.sender = factory.createAddress();
        this.origin = factory.createAddress();
        this.receiver = factory.createAddress();

        subType = new MutableUnion<>(factory.getMessageTypes(), factory);
        headers = new MutableSet<>(factory.getHeaderTypes(), factory);
    }

    public <T extends Mutable<T>> Message(GossipFactory factory,
                                          MessageType<T> msgType,
                                          Consumer<T> consumer) {
        this(factory);

        consumer.accept(subType.activate(msgType));
    }

    public Message(GossipFactory factory,
                   MessageType<?> msgType) {
        this(factory);

        subType.activate(msgType);
    }

    @Override
    public void copyFrom(Message obj) {
        if (obj == null) {
            id = 0;
            timestamp = 0;
            sender.clear();
            origin.clear();
            receiver.clear();
            subType.clear();
            headers.clear();
            return;
        }

        id = obj.id;
        timestamp = obj.timestamp;
        sender.copyFrom(obj.sender);
        origin.copyFrom(obj.origin);
        receiver.copyFrom(obj.receiver);
        subType.copyFrom(obj.subType);
        headers.copyFrom(obj.headers);
    }

    public void deserialize(ByteBuffer buffer) {
        id = buffer.getLong();
        timestamp = buffer.getLong();
        receiver.deserialize(buffer);
        sender.deserialize(buffer);
        origin.deserialize(buffer);
        headers.deserialize(buffer);
        subType.deserialize(buffer);
    }

    public void serialize(ByteBuffer buffer) {
        buffer.putLong(id);
        buffer.putLong(timestamp);
        receiver.serialize(buffer);
        sender.serialize(buffer);
        origin.serialize(buffer);
        headers.serialize(buffer);
        subType.serialize(buffer);
    }

    public boolean instanceOf(MessageType<?> MessageType) {
        return subType.isActive(MessageType);
    }

    public <T extends Mutable<T>> T castTo(MessageType<T> messageType) {
        return subType.get(messageType);
    }

    public <T extends Mutable<T>> T activate(MessageType<T> messageType) {
        return subType.activate(messageType);
    }

    public <T extends Mutable<T>> T getHeader(HeaderType<T> headerType) {
        return headers.get(headerType);
    }

    public boolean hasHeader(HeaderType<?> headerType) {
        return headers.isActive(headerType);
    }

    public String toString() {
        String idStr = Long.toUnsignedString(id);
        if (idStr.length() > 4) {
            idStr = idStr.substring(0, 4);
        }

        return idStr + ":" + this.getSubType().toString();
    }
}
