package node.datagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.channels.DatagramChannel;

@ToString(exclude = {"gossipNode", "channel"})
@Getter
@EqualsAndHashCode(of = "address")
public class Party {
    private final GossipNode gossipNode;
    private final Address address;
    private final DatagramChannel channel;

    public Party(Address address, GossipNode gossipNode, DatagramChannel channel) {
        this.address = gossipNode.getFactory().createAddress();
        this.address.copyFrom(address);
        this.channel = channel;
        if (!address.isSet()) {
            throw new RuntimeException("Address is not set");
        }
        this.gossipNode = gossipNode;
    }
}
