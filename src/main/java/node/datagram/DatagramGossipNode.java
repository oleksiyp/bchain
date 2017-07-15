package node.datagram;

import node.datagram.shared.GossipNodeShared;
import node.datagram.event.RegisterListenerEvent;
import node.datagram.event.SendEvent;
import util.MappedQueue;

import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static node.datagram.MessageType.PING_MESSAGE_TYPE;
import static node.datagram.event.EventType.REGISTER_LISTENER_EVENT;
import static node.datagram.event.EventType.REGISTER_PARTY_EVENT;
import static node.datagram.event.EventType.SEND_EVENT;

public class DatagramGossipNode implements GossipNode {
    private final GossipNodeShared shared;
    private final Map<MessageType<?>, List<Consumer<Message>>> listeners;

    private Address publicAddress;
    private Address listenAddress;

    private Ledger ledger;
    private final Party selfParty;
    private RemoteParties remoteParties;

    public DatagramGossipNode(
            GossipNodeShared shared,
            InetAddress publicAddress,
            InetAddress listenAddress,
            Supplier<Integer> portRng,
            int ledgerSize,
            long ledgerRetentionTime,
            Supplier<Message> messageFactory) throws IOException {
        this.shared = shared;

        selfParty = tryBindingPorts(portRng, (port) -> {
            Address address = new Address();
            address.copyFrom(new InetSocketAddress(listenAddress, port));
            try {
                DatagramChannel channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.bind(address.toInetSocketAddress());
                return new Party(address, this, channel);
            } catch (IOException e) {
                System.out.println(e);
                throw new IOError(e);
            }
        });

        shared.getPartyRegistrar().accept(selfParty);

        this.listenAddress = selfParty.getAddress();

        this.publicAddress = new Address();
        this.publicAddress.copyFrom(new InetSocketAddress(publicAddress, this.listenAddress.getPort()));

        ledger = new Ledger(new MappedQueue<>(
                ledgerSize,
                ledgerRetentionTime,
                messageFactory,
                msg -> msg.copyFrom(null)

        ));

        remoteParties = new RemoteParties(this, shared, selfParty.getChannel());
        listeners = new HashMap<>();
    }

    protected Party tryBindingPorts(Supplier<Integer> portRng, Function<Integer, Party> bindFunc) {
        while (true) {
            Integer port = portRng.get();
            if (port == null) {
                throw new RuntimeException("failed to bind");
            }

            try {
                return bindFunc.apply(port);
            } catch (IOError ex) {
                // skip
            }
        }
    }


    @Override
    public Address address() {
        return publicAddress;
    }

    @Override
    public void join(Address address) {
        shared
                .getReadProcessDispatcher()
                .dispatch(2, (i, event) -> {
                    event.setShared(shared);
                    event.setSelf(selfParty);
                    if (i == 0) {
                        event.setSelf(selfParty);
                        event
                                .activateSubEvent(REGISTER_PARTY_EVENT)
                                .getAddress()
                                .copyFrom(address);
                    } else if (i == 1) {
                        SendEvent sendEvent = event.activateSubEvent(SEND_EVENT);
                        sendEvent.getReceiverAddress().copyFrom(address);
                        Message message = sendEvent.getMessage();
                        message.activateSubType(PING_MESSAGE_TYPE);
                        message.getOrigin().copyFrom(publicAddress);
                    }
                });
    }


    @Override
    public void listen(MessageType<?> messageType,
                       Consumer<Message> consumer) {
        shared
                .getReadProcessDispatcher()
                .dispatch(1, (i, event) -> {
                    RegisterListenerEvent listenerEvent = event.getSubEvent().activate(REGISTER_LISTENER_EVENT);
                    event.setSelf(selfParty);
                    event.setShared(shared);
                    listenerEvent.setAdd(true);
                    listenerEvent.setMessageType(messageType);
                    listenerEvent.setListener(consumer);
                });
    }

    @Override
    public void send(Message msg) {
        shared.getReadProcessDispatcher()
                .dispatch(1, (i, event) -> {
            event.setSelf(selfParty);
            event.setShared(shared);

            SendEvent sendEvent = event.getSubEvent().activate(SEND_EVENT);
            Message message = sendEvent.getMessage();
            message.copyFrom(msg);
            message.getOrigin().copyFrom(publicAddress);
        });
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public RemoteParties getRemoteParties() {
        return remoteParties;
    }

    @Override
    public Map<MessageType<?>, List<Consumer<Message>>> getListeners() {
        return listeners;
    }
}
