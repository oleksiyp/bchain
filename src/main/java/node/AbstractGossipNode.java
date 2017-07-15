package node;

import io.netty.channel.Channel;
import util.VolatileCollection;
import util.VolatileMap;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractGossipNode {
    protected final InetAddress publicAddress;
    protected final InetAddress listenAddress;

    protected int port;

    protected final VolatileCollection<Consumer<Message>> listeners;
    protected final VolatileMap<String, Channel> channels;
    protected final VolatileCollection<Consumer<InetSocketAddress>> joinedListeners;
    protected final VolatileCollection<Consumer<InetSocketAddress>> resignedListeners;

    public AbstractGossipNode(InetAddress publicAddress, InetAddress listenAddress) {
        this.publicAddress = publicAddress;
        this.listenAddress = listenAddress;

        listeners = new VolatileCollection<>(ArrayList::new);
        channels = new VolatileMap<>(HashMap::new);
        joinedListeners = new VolatileCollection<>(ArrayList::new);
        resignedListeners = new VolatileCollection<>(ArrayList::new);

    }

    protected void tryBindingPorts(Supplier<Integer> portRng, Consumer<Integer> bindFunc) {
        while (true) {
            Integer port = portRng.get();
            if (port == null) {
                throw new RuntimeException("failed to bind");
            }

            try {
                bindFunc.accept(port);
                this.port = port;
                return;
            } catch (RuntimeException ex) {
                // skip
            }
        }
    }


}

