package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.Address;
import node.GossipNode;
import node.Message;
import node.RemoteParties;
import node.datagram.*;
import node.datagram.event.Event;
import node.datagram.event.RegisterPartyEvent;
import node.datagram.event.SendEvent;
import node.datagram.event.WriteEvent;
import node.datagram.DatagramGossipNodeShared;

import java.util.List;
import java.util.function.BiConsumer;

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
        DatagramGossipNodeShared shared = event.getShared();

        if (event.isSubEventActive(SendEvent.SEND_EVENT)) {
            SendEvent sendEvent = event.getSubEvent(SendEvent.SEND_EVENT);

            Message message = sendEvent.getMessage();

            RemoteParties parties = gossipNode.getRemoteParties();
            parties.register(message.getSender());
            List<Party> list = parties.getList();

            if (message.getReceiver().isSet()) {
                if (message.getReceiver().equals(gossipNode.address())) {
                    return;
                }
            }

            int n = countPartiesToSend(sendEvent, list);

            publisher.init(gossipNode, sendEvent, party, shared);
            shared.getWriteDispatcher().dispatch(n, publisher);
            publisher.init(null, null, null, null);

        } else if (event.isSubEventActive(RegisterPartyEvent.REGISTER_PARTY_EVENT)) {
            RegisterPartyEvent registerPartyEvent = event.getSubEvent(RegisterPartyEvent.REGISTER_PARTY_EVENT);

            gossipNode.getRemoteParties()
                    .register(registerPartyEvent.getAddress());
        }
    }

    public static class Publisher implements BiConsumer<Integer, Event> {
        int it;
        private GossipNode gossipNode;
        private SendEvent sendEvent;
        private Party party;
        private DatagramGossipNodeShared shared;
        private List<Party> partyList;

        private void init(GossipNode gossipNode, SendEvent sendEvent, Party party, DatagramGossipNodeShared shared) {
            it = 0;
            this.party = party;
            this.shared = shared;
            this.gossipNode = gossipNode;
            this.sendEvent = sendEvent;

            if (gossipNode != null) {
                partyList = gossipNode.getRemoteParties().getList();
            } else {
                partyList = null;
            }
        }

        @Override
        public void accept(Integer i, Event event) {

            Party next = partyList.get(it++);

            while (!filterParty(sendEvent.getMessage(), next.getAddress())) {
                next = partyList.get(it++);
            }

            event.setSelf(party);
            event.setShared(shared);

            WriteEvent writeEvent = event.getSubEvent().activate(WriteEvent.TYPE);

            writeEvent.setTo(next);
            Message message = writeEvent.getMessage();

            message.copyFrom(sendEvent.getMessage());
            message.getSender().copyFrom(gossipNode.address());
        }
    }

    private int countPartiesToSend(SendEvent sendEvent, List<Party> parties) {
        int n = 0;
        for (int i = 0; i < parties.size(); i++) {
            Party next = parties.get(i);
            if (!filterParty(sendEvent.getMessage(), next.getAddress())) {
                continue;
            }
            n++;
        }
        return n;
    }

    private static boolean filterParty(Message message, Address address) {
        if (message.getReceiver().isSet()) {
            if (!address.equals(message.getReceiver())) {
                return false;
            }
        } else {
            if (address.equals(message.getSender())) {
                return false;
            }
        }
        return true;
    }
}
