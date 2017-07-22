package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.Address;
import node.Party;
import node.factory.GossipFactory;
import util.mutable.Mutable;

@Getter
@Setter
@ToString(of = "party")
public class RegisterPartyEvent implements Mutable<RegisterPartyEvent> {
    public static final EventType<RegisterPartyEvent> REGISTER_PARTY_EVENT = new EventType<>(
            "REGISTER_PARTY_EVENT",
            RegisterPartyEvent.class);

    private Party party;

    @Override
    public void copyFrom(RegisterPartyEvent obj) {
        if (obj == null) {
            party = null;
            return;
        }
        party = obj.party;
    }
}
