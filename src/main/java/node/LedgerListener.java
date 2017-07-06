package node;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public interface LedgerListener {
    void notifyListeners(Message message);

    void broadcastChannels(Message message, ChannelId id);

    void sendChannel(ChannelId id, Message message);
}
