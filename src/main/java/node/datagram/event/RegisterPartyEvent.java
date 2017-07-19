package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.Address;
import node.factory.GossipFactory;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class RegisterPartyEvent implements Mutable<RegisterPartyEvent> {
    public static final EventType<RegisterPartyEvent> REGISTER_PARTY_EVENT = new EventType<>(
            "REGISTER_PARTY_EVENT",
            RegisterPartyEvent.class
    );
    private final Address address;

    public RegisterPartyEvent(GossipFactory factory) {
        address = factory.createAddress();
    }

    @Override
    public void copyFrom(RegisterPartyEvent obj) {
        if (obj == null) {
            address.clear();
            return;
        }
        obj.address.copyFrom(obj.address);
    }
}
