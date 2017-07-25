package gossip.message;

import gossip.Clearable;
import gossip.in_out.Serializable;
import gossip.TypeAware;

public interface Message extends Serializable, Clearable, TypeAware {
    long getId();
}
