package node2.message;

import node2.Clearable;
import node2.in_out.Serializable;
import node2.TypeAware;

public interface Message extends Serializable, Clearable, TypeAware {
    long getId();
}
