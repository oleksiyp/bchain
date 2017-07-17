package node.datagram;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import node.datagram.shared.GossipNodeShared;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.*;

@ToString(of = "partyList", includeFieldNames = false)
@Slf4j
public class RemoteParties {
    private final GossipNode gossipNode;
    private final GossipNodeShared shared;
    private final Map<Address, Party> partyMap;
    private final List<Party> partyList;

    public RemoteParties(GossipNode gossipNode,
                         GossipNodeShared shared) {

        this.gossipNode = gossipNode;
        this.shared = shared;
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

        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.connect(address.toInetSocketAddress());

            Party party = new Party(address, gossipNode, channel);

            log.info("Registering {} at {}", party, gossipNode.address());
            if (partyMap.put(party.getAddress(), party) == null) {
                partyList.add(party);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Party> getList() {
        return partyList;
    }
}
