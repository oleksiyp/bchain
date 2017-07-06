package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

class InetSocketAddressSerializer extends Serializer<InetSocketAddress> {

    @Override
    public void write(Kryo kryo, Output output, InetSocketAddress object) {
        InetAddress address = object.getAddress();
        output.writeInt(address.getAddress().length);
        output.write(address.getAddress());
        output.writeShort(object.getPort());
    }

    @Override
    public InetSocketAddress read(Kryo kryo, Input input, Class<InetSocketAddress> type) {
        int len = input.readInt();
        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(input.readBytes(len));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        int port = input.readShortUnsigned();
        return new InetSocketAddress(address, port);
    }
}
