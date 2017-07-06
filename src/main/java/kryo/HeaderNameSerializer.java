package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import node.Headers.HeaderName;

public class HeaderNameSerializer extends Serializer<HeaderName> {
    @Override
    public void write(Kryo kryo, Output output, HeaderName object) {
        output.writeInt(object.getTag());
    }

    @Override
    public HeaderName read(Kryo kryo, Input input, Class<HeaderName> type) {
        int tag = input.readInt();
        HeaderName<?> headerName = HeaderName.ALL_HEADERS.get(tag);
        if (headerName == null) {
            throw new RuntimeException("bad tag: " + tag);
        }
        return headerName;
    }
}
