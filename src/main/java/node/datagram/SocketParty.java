package node.datagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import node.Address;
import node.GossipNode;
import node.Message;
import node.Party;
import util.mutable.MutableBlockingQueue;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Getter
@EqualsAndHashCode(of = "address")
public class SocketParty implements Party {
    private final GossipNode gossipNode;
    private final Address address;
    private final SocketChannel channel;
    private ByteBuffer readBuffer;
    private MutableBlockingQueue<Message> writeQ;
    @Setter
    private SelectionKey selectionKey;
    private final Message writeMsg;

    public SocketParty(Address address, GossipNode gossipNode, SocketChannel channel, MutableBlockingQueue<Message> writeQ) {
        this.address = gossipNode.getFactory().createAddress();
        this.address.copyFrom(address);
        this.channel = channel;
        if (!address.isSet()) {
            throw new RuntimeException("Address is not set");
        }
        this.gossipNode = gossipNode;
        this.writeQ = writeQ;
        this.writeMsg = gossipNode.getFactory().createMessage();
    }

    @Override
    public String toString() {
        return address.toString();
    }

    public void allocateReadBuffer(int bufSize) {
        readBuffer = ByteBuffer.allocateDirect(bufSize);
    }

    public MutableBlockingQueue<Message> writeQ() {
        return writeQ;
    }

    public void writeInterested() {
        int ops = selectionKey.interestOps();
        selectionKey.interestOps(SelectionKey.OP_WRITE | ops);
    }

    public void notInterestedInWrite() {
        int ops = selectionKey.interestOps();
        selectionKey.interestOps(SelectionKey.OP_WRITE | ops);
    }

}
