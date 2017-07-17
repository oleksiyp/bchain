package node.datagram;

import node.datagram.ledger.Ledger;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GossipNode extends Gossip {
    GossipFactory getFactory();

    Ledger getLedger();

    RemoteParties getRemoteParties();

    Map<MessageType<?>,List<Consumer<Message>>> getListeners();
}
