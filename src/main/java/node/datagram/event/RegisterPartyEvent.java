package node.datagram.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.datagram.Address;
import util.mutable.Mutable;

@Getter
@Setter
@ToString
public class RegisterPartyEvent implements Mutable<RegisterPartyEvent> {
    private final Address address;

    public RegisterPartyEvent() {
        address = new Address();
    }

    @Override
    public void copyFrom(RegisterPartyEvent obj) {
        if (obj == null) {
            address.copyFrom(null);
            return;
        }
        obj.address.copyFrom(obj.address);
    }
}
