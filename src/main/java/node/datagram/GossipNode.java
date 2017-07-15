package node.datagram;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GossipNode extends Gossip {
    Ledger getLedger();

    RemoteParties getRemoteParties();

    Map<MessageType<?>,List<Consumer<Message>>> getListeners();
}
