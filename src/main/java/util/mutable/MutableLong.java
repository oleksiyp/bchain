package util.mutable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.Serializable;

import java.nio.ByteBuffer;

@Getter
@Setter
@ToString(includeFieldNames = false)
@EqualsAndHashCode
public class MutableLong implements Mutable<MutableLong>, Serializable {
    private long value;

    @Override
    public void copyFrom(MutableLong obj) {
        if (obj == null) {
            value = 0;
            return;
        }
        value = obj.value;
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        value = buffer.getLong();
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putLong(value);
    }
}
