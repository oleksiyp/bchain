package node.pong;

import lombok.ToString;
import node.GossipFactory;
import util.Serializable;
import util.mutable.Mutable;

import java.nio.ByteBuffer;

@ToString
public class PingMessage implements Mutable<PingMessage>, Serializable {
    @Override
    public void copyFrom(PingMessage obj) {
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
    }

    @Override
    public void serialize(ByteBuffer buffer) {
    }
}
