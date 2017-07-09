package node.ledger;

import io.netty.channel.ChannelId;
import node.Headers;
import node.Message;
import node.discovery2.AckRequestNodeCountMessage;
import node.discovery2.RequestNodeCountMessage;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class LedgerActor {
    long id;
    Message message;
    ChannelId receiveChannelId;
    LedgerListener ledgerListener;
    Headers headers;

    public LedgerActor() {
    }

    public void accept(ProcessMessageEvent event) {
        if (!event.getMessage().getHeaders().isRouteBack()) {
            message = event.getMessage();
            headers = message.getHeaders();
            receiveChannelId = event.getReceiveChannelId();
            ledgerListener.notifyListeners(message);
            ledgerListener.broadcastChannels(message, receiveChannelId.asLongText());
        } else {
            if (receiveChannelId != null) {
                ledgerListener.sendChannel(receiveChannelId.asLongText(), event.getMessage());
            }
        }

        //        if (message instanceof RequestNodeCountMessage) {
//            nodeCounter.put(message.getHeaders().getId(), 0);
//            spanningCounters.put(message.getHeaders().getId(), 1);
//        } else if (message instanceof AckRequestNodeCountMessage) {
//            Long id = message.getHeaders().get(Headers.ROUTE_BACK_ID);
//            if (id != null) {
//                spanningCounters.put(id, spanningCounters.getOrDefault(id, 0) + 1);
//            }
//            return;
//        } else if (message instanceof NodeCountMessage) {
//            Long id = message.getHeaders().get(Headers.ROUTE_BACK_ID);
//            if (id != null) {
//                NodeCountMessage countMessage = (NodeCountMessage) message;
//                nodeCounter.put(id, nodeCounter.get(id) + countMessage.getCount());
//                spanningCounters.put(id, spanningCounters.getOrDefault(id, 0) - 1);
//                if (spanningCounters.get(id) == 0) {
//                    countMessage = new NodeCountMessage(nodeCounter.get(id));
//                    countMessage.getHeaders().set(Headers.ROUTE_BACK_ID, id);
//                    ChannelId channel = channelIndex.get(id);
//                    if (channel == null) {
//                        return;
//                    }
//                    ledgerListener.sendChannel(channel, countMessage);
//                }
//                return;
//            }
//        }

    }

    public void accept(ReplayLedgerEvent event) {
        event.getListener().accept(message);
    }

    public void clear() {
        id = 0;
        message = null;
        receiveChannelId = null;
    }

    public void setLedgerListener(LedgerListener ledgerListener) {
        this.ledgerListener = ledgerListener;
    }
//        int nodeCounter;
//        int spanningCounters;

}
