package node.datagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import node.Address;
import node.GossipNode;
import node.Party;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Getter
@EqualsAndHashCode(of = "address")
public class ServerSocketParty implements Party {
    private final GossipNode gossipNode;
    private final Address address;
    private final ServerSocketChannel channel;

    public ServerSocketParty(Address address, GossipNode gossipNode, ServerSocketChannel channel) {
        this.address = gossipNode.getFactory().createAddress();
        this.address.copyFrom(address);
        this.channel = channel;
        if (!address.isSet()) {
            throw new RuntimeException("Address is not set");
        }
        this.gossipNode = gossipNode;
    }

    @Override
    public String toString() {
        return "SERVER:" + address.toString();
    }
}
