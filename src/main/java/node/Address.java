package node;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import util.Serializable;
import util.mutable.Mutable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;


@EqualsAndHashCode(exclude = "socketAddress")
public class Address implements Mutable<Address>, Serializable {
    @Getter private boolean set;
    @Getter private boolean ipv6;
    @Getter private byte[] data;
    @Getter private int port;
    InetSocketAddress socketAddress;

    public Address() {
        data = new byte[16];
    }

    @Override
    public void copyFrom(Address obj) {
        if (obj == null) {
            set = false;
            ipv6 = false;
            fill(data, (byte) 0);
            port = 0;
            socketAddress = null;
            return;
        }

        set = obj.set;
        ipv6 = obj.ipv6;
        for (int i = 0; i < (len()); i++) {
            data[i] = obj.data[i];
        }
        port = obj.port;
        socketAddress = obj.socketAddress;
    }

    @Override
    public void copyFromObj(Object obj) {
        if (obj instanceof InetSocketAddress)  {
            set = true;
            socketAddress = null;
            InetSocketAddress sockAddr = (InetSocketAddress) obj;
            InetAddress address = sockAddr.getAddress();
            byte[] addrData = address.getAddress();
            ipv6 = addrData.length == 16;
            for (int i = 0; i < data.length; i++) {
                if (i < len()) {
                    data[i] = addrData[i];
                } else {
                    data[i] = 0;
                }
            }
            port = sockAddr.getPort();
        } else if (obj instanceof Address) {
            copyFrom((Address) obj);
        } else {
            throw new UnsupportedOperationException("copyFromObj: " + obj);
        }
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        if (buffer.get() == 1) {
            set = true;
            ipv6 = buffer.get() == 1;
            buffer.get(data, 0, len());
            port = buffer.getShort() & 0xffff;
        } else {
            clear();
        }
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put(toByte(set));
        if (set) {
            buffer.put(toByte(ipv6));
            buffer.put(data, 0, len());
            buffer.putShort((short) (port & 0xffff));
        }
    }

    private int len() {
        return ipv6 ? 16 : 4;
    }

    private byte toByte(boolean set) {
        return (byte) (set ? 1 : 0);
    }

    public InetSocketAddress toInetSocketAddress() {
        if (set) {
            if (socketAddress == null) {
                try {
                    InetAddress addr = InetAddress.getByAddress(copyOf(data, len()));
                    socketAddress = new InetSocketAddress(addr, port);
                } catch (IOException ex) {
                    socketAddress = null;
                }
            }
        } else {
            socketAddress = null;
        }
        return socketAddress;
    }

    public void setSet(boolean set) {
        this.set = set;
        socketAddress = null;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
        socketAddress = null;
    }

    public void setPort(int port) {
        this.port = port;
        socketAddress = null;
    }

    @Override
    public String toString() {
        return set ? ":" + port : "-";
    }
}
