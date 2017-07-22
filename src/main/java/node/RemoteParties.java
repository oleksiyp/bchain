package node;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import node.datagram.SocketGossipNodeShared;
import node.datagram.SocketParty;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ToString(of = "partyList", includeFieldNames = false)
@Slf4j
public class RemoteParties {
    private final GossipNode gossipNode;
    private final Map<Address, Party> partyMap;
    private final List<Party> partyList;
    private static AtomicInteger val = new AtomicInteger();

    public RemoteParties(GossipNode gossipNode) {
        this.gossipNode = gossipNode;
        partyMap = new HashMap<>();
        partyList = new ArrayList<>();
    }

    public void register(Party party) {
        Address address = party.getAddress();
        if (!address.isSet()) {
            return;
        }

        if (partyMap.containsKey(address)) {
            return;
        }

        log.info("Registering {} at {}", party, gossipNode.address());
        if (partyMap.put(party.getAddress(), party) == null) {
            partyList.add(party);
        }
    }

    public List<Party> getList() {
        return partyList;
    }
}
