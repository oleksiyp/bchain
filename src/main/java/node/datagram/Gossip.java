package node.datagram;

import util.Cancelable;
import util.mutable.Mutable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Gossip {
    Address address();

//    int nChannels();

    void join(Address address);

//    Cancelable listenMembership(
//            Consumer<InetSocketAddress> joined,
//            Consumer<InetSocketAddress> resigned);

    void send(Message msg);

    Cancelable listen(MessageType<?> messageType,
                      Consumer<Message> consumer);

    <T extends Mutable<T>> Cancelable listen(MessageType<T> messageType,
                                             BiConsumer<Message, T> consumer);


//    void routeBack(node.Message message);

//    Cancelable listen(boolean replayLedger, Consumer<Message> listener);
}
