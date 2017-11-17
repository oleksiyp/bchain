package gossip;

import gossip.message.Message;

public interface MessageHandler {
    void receive(SocketParty party, Message message);
}
