package node.discovery;

import node.Message;

import java.net.InetSocketAddress;

public class JoinRequestMessage extends Message {
    private final InetSocketAddress address;

    public JoinRequestMessage() {
        super();
        address = null;
    }

    public JoinRequestMessage(InetSocketAddress address) {
        super();
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
