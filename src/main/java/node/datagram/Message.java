package node.datagram;

import lombok.Getter;
import lombok.ToString;
import util.Serializable;
import util.mutable.Mutable;
import util.mutable.MutableSet;
import util.mutable.MutableUnion;

import java.nio.ByteBuffer;

@Getter
@ToString(of = "subType", includeFieldNames = false)
public class Message implements Mutable<Message>, Serializable {
    private long id;
    private long timestamp;
    private final Address sender;
    private final Address receiver;
    private final Address origin;

    private MutableUnion<MessageType<?>> subType;
    private MutableSet<HeaderType<?>> headers;

    public Message() {
        this.sender = new Address();
        this.origin = new Address();
        this.receiver = new Address();

        subType = new MutableUnion<>(MessageType.ALL);
        headers = new MutableSet<>(HeaderType.ALL);
    }


    @Override
    public void copyFrom(Message obj) {
        if (obj == null) {
            id = 0;
            timestamp = 0;
            sender.copyFrom(null);
            origin.copyFrom(null);
            receiver.copyFrom(null);
            subType.copyFrom(null);
            headers.copyFrom(null);
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

    public boolean isSubTypeActive(MessageType<?> MessageType) {
        return subType.isActive(MessageType);
    }

    public <T extends Mutable<T>> T getSubType(MessageType<T> MessageType) {
        return subType.get(MessageType);
    }

    public <T extends Mutable<T>> T activateSubType(MessageType<T> MessageType) {
        return subType.activate(MessageType);
    }
}
