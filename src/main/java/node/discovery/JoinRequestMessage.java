package node.discovery;

import node.Message;

import java.net.InetSocketAddress;

public class JoinRequestMessage extends Message {
    private final InetSocketAddress address;

    JoinRequestMessage() {
        address = null;
    }

    public JoinRequestMessage(InetSocketAddress address) {
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
