package node;

import util.mutable.ChoiceType;
import util.mutable.Mutable;
import util.mutable.MutableLong;

public class HeaderType<T extends Mutable<T>> extends ChoiceType<T> {
    public static final HeaderType<MutableLong> REFERENCE_ID = new HeaderType<>(
            "REFERENCE_ID",
            MutableLong.class);

    public HeaderType(String name, Class<T> type) {
        super(name, type);
    }
}
