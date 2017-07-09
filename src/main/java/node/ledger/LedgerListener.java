package node.ledger;

import io.netty.channel.ChannelId;
import node.Gossip;
import node.Message;
import util.Pool;

public interface LedgerListener extends Gossip {
    void notifyListeners(Message message);

    void broadcastChannels(Message message, String id);

    void sendChannel(String id, Message message);

    Pool getPool();
}
