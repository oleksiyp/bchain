package node;

import util.Cancelable;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

import static node.Headers.ROUTE_BACK_TARGET;

public interface Gossip {
    InetSocketAddress address();

    int nChannels();

    void join(InetSocketAddress address);

    Cancelable listenMembership(
            Consumer<InetSocketAddress> joined,
            Consumer<InetSocketAddress> resigned);

    void send(Message msg);

    void routeBack(Message message);

    Cancelable listen(boolean replayLedger, Consumer<Message> listener);

    default <T extends Message> Cancelable listen(boolean replayLedger, Class<T> filter, Consumer<T> listener) {
        return listen(replayLedger, message -> {
            if (filter.isAssignableFrom(message.getClass())) {
                listener.accept(filter.cast(message));
            }
        });
    }

    default <T extends Message> Cancelable routeBackListener(boolean replayLedger, Class<T> filter, Consumer<T> listener) {
        return listen(replayLedger, filter, message -> {
            InetSocketAddress routeBackTarget = message.getHeaders().get(ROUTE_BACK_TARGET);
            if (routeBackTarget == null) {
                return;
            }
            if (!address().equals(routeBackTarget)) {
                return;
            }

            listener.accept(message);
        });
    }

}
