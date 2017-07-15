package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.GossipNode;
import node.datagram.Party;
import node.datagram.RemoteParties;
import node.datagram.event.Event;
import node.datagram.event.RegisterPartyEvent;
import node.datagram.event.SendEvent;
import node.datagram.event.WriteEvent;
import node.datagram.shared.GossipNodeShared;

import java.util.List;
import java.util.function.BiConsumer;

import static node.datagram.event.EventType.*;

public class BroadcastHandler implements EventHandler<Event> {
    private final Publisher publisher;

    public BroadcastHandler() {
        publisher = new Publisher();
    }

    @Override
    public void onEvent(Event event,
                        long sequence,
                        boolean endOfBatch) throws Exception {

        Party party = event.getSelf();
        GossipNode gossipNode = party.getGossipNode();
        GossipNodeShared shared = event.getShared();

        if (event.isSubEventActive(SEND_EVENT)) {
            SendEvent sendEvent = event.getSubEvent(SEND_EVENT);
            if (sendEvent.isInLedger()) {
                return;
            }

            RemoteParties remoteParties = gossipNode.getRemoteParties();
            remoteParties.register(sendEvent.getSenderAddress());
            List<Party> list = remoteParties.getList();

            int n = countPartiesToSend(sendEvent, list);

            publisher.init(gossipNode, sendEvent);
            shared.getWriteDispatcher().dispatch(n, publisher);
            publisher.init(null, null);

        } else if (event.isSubEventActive(REGISTER_PARTY_EVENT)) {
            RegisterPartyEvent registerPartyEvent = event.getSubEvent(REGISTER_PARTY_EVENT);

            gossipNode.getRemoteParties()
                    .register(registerPartyEvent.getAddress());
        }
    }

    public static class Publisher implements BiConsumer<Integer, Event> {
        int it;
        private GossipNode gossipNode;
        private SendEvent sendEvent;

        private void init(GossipNode gossipNode, SendEvent sendEvent) {
            it = 0;
            this.gossipNode = gossipNode;
            this.sendEvent = sendEvent;
        }

        @Override
        public void accept(Integer i, Event event) {
            RemoteParties remoteParties = gossipNode.getRemoteParties();
            List<Party> list = remoteParties.getList();

            Party next = list.get(it++);

            while (!filterParty(sendEvent, next)) {
                next = list.get(it++);
            }

            WriteEvent writeEvent = event.getSubEvent().activate(WRITE_EVENT);

            writeEvent.setParty(next);
            writeEvent.getMessage().getSender().copyFrom(gossipNode.address());
            writeEvent.getMessage().copyFrom(sendEvent.getMessage());
        }
    }

    private int countPartiesToSend(SendEvent sendEvent, List<Party> parties) {
        int n = 0;
        for (int i = 0; i < parties.size(); i++) {
            Party next = parties.get(i);
            if (!filterParty(sendEvent, next)) {
                continue;
            }
            n++;
        }
        return n;
    }

    private static boolean filterParty(SendEvent sendEvent, Party party) {
        if (party.getAddress().equals(sendEvent.getSenderAddress())) {
            return false;
        }
        if (sendEvent.getReceiverAddress().isSet()) {
            if (!party.getAddress().equals(sendEvent.getReceiverAddress())) {
                return false;
            }
        }
        return true;
    }
}
