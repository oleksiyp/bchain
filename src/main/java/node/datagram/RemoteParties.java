package node.datagram;

import lombok.ToString;
import node.datagram.shared.GossipNodeShared;

import java.nio.channels.DatagramChannel;
import java.util.*;

@ToString(of = "partyMap", includeFieldNames = false)
public class RemoteParties {
    private final GossipNode gossipNode;
    private final GossipNodeShared shared;
    private final DatagramChannel channel;
    private final Map<Address, Party> partyMap;
    private final List<Party> partyList;

    public RemoteParties(GossipNode gossipNode,
                         GossipNodeShared shared,
                         DatagramChannel channel) {

        this.gossipNode = gossipNode;
        this.shared = shared;
        this.channel = channel;
        partyMap = new HashMap<>();
        partyList = new ArrayList<>();
    }

    public void register(Address address) {
        if (!address.isSet()) {
            return;
        }

        if (partyMap.containsKey(address)) {
            return;
        }

        Party party = new Party(address, gossipNode, channel);

        partyMap.put(party.getAddress(), party);
        partyList.add(party);

        shared.getPartyRegistrar().accept(party);
    }

    public List<Party> getList() {
        return partyList;
    }
}
