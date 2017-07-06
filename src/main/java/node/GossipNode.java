package node;

import com.esotericsoftware.kryo.Kryo;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import kryo.KryoDecoder;
import kryo.KryoEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Cancelable;
import util.VolatileCollection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.netty.handler.codec.serialization.ClassResolvers.softCachingConcurrentResolver;

public class GossipNode implements Gossip, LedgerListener, Cancelable {
    private static final Logger log = LogManager.getLogger(GossipNode.class);

    private final InetAddress addr;
    private final InetAddress listenAddress;
    private final int port;

    private final ServerBootstrap server;
    private final Bootstrap client;

    private final VolatileCollection<Consumer<Message>> listeners;
    private final VolatileCollection<Channel> channels;
    private final VolatileCollection<Consumer<InetSocketAddress>> joinedListeners;
    private final VolatileCollection<Consumer<InetSocketAddress>> resignedListeners;

    private final Ledger ledger;

    public GossipNode(Pools pools,
                      InetAddress publicAddress,
                      InetAddress listenAddress,
                      Supplier<Integer> portSeq,
                      Ledger ledger,
                      Consumer<Kryo> kryoConfigurer) {

        ledger.setListener(this);
        this.addr = publicAddress;
        this.listenAddress = listenAddress;
        this.ledger = ledger;

        server = new ServerBootstrap();
        client = new Bootstrap();

        server.group(pools.bossGroup, pools.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelListMaintainer())
                                .addLast(new LoggingHandler())
                                .addLast(new KryoEncoder(kryoConfigurer))
                                .addLast(new KryoDecoder(kryoConfigurer))
//                                .addLast(new LoggingHandler(LogLevel.ERROR))
                                .addLast(new LedgerHandler())
                                .addLast(new ExceptionHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);


        client.group(pools.clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelListMaintainer())
                                .addLast(new LoggingHandler())
                                .addLast(new KryoEncoder(kryoConfigurer))
                                .addLast(new KryoDecoder(kryoConfigurer))
//                                .addLast(new LoggingHandler(LogLevel.ERROR))
                                .addLast(new LedgerHandler())
                                .addLast(new ExceptionHandler());
                    }
                });

        port = bindPort(portSeq);

        listeners = new VolatileCollection<>(ArrayList::new);
        channels = new VolatileCollection<>(HashSet::new);
        joinedListeners = new VolatileCollection<>(ArrayList::new);
        resignedListeners = new VolatileCollection<>(ArrayList::new);
    }

    private int bindPort(Supplier<Integer> portRng) {
        while (true) {
            int port = portRng.get();

            ChannelFuture future = server
                    .bind(listenAddress, port);

            sync(future);

            if (future.isSuccess()) {
                return port;
            }
        }
    }

    @Override
    public InetSocketAddress address() {
        return new InetSocketAddress(addr, port);
    }

    @Override
    public int nChannels() {
        return channels.size();
    }

    @Override
    public void join(InetSocketAddress address) {
        if (address().equals(address)) {
            return;
        }

        if (!sync(client.connect(address))
                .isSuccess()) {
            throw new RuntimeException("connect");
        }
    }

    @Override
    public Cancelable listenMembership(Consumer<InetSocketAddress> joined,
                                       Consumer<InetSocketAddress> resigned) {

        if (joined != null) {
            joinedListeners.add(joined);
        }
        if (resigned != null) {
            resignedListeners.add(resigned);
        }
        return () -> {
            if (joined != null) {
                joinedListeners.remove(joined);
            }
            if (resigned != null) {
                resignedListeners.remove(resigned);
            }
        };
    }

    @Override
    public void send(Message message) {
        Headers headers = message.getHeaders();

        headers.setOriginator(address());
        headers.setSender(address());

        ledger.sendMessage(message, null);
    }

    @Override
    public void notifyListeners(Message message) {
        for (Consumer<Message> listener : listeners) {
            listener.accept(message);
        }
    }

    @Override
    public void sendChannel(ChannelId id, Message message) {
        message.getHeaders().setSender(address());

        for (Channel ch : channels) {
            if (!ch.id().equals(id)) {
                continue;
            }
            if (!ch.isActive()) {
                continue;
            }

            ch.writeAndFlush(message);
        }
    }

    @Override
    public void broadcastChannels(Message message, ChannelId recieveChannelId) {
        message.getHeaders().setSender(address());

        for (Channel ch : channels) {
            if (recieveChannelId != null &&
                    recieveChannelId.equals(ch.id())) {
                continue;
            }
            ch.writeAndFlush(message);
        }
    }

    @Override
    public void routeBack(Message message) {
        Headers headers = message.getHeaders();
        headers.setOriginator(address());
        headers.setSender(address());

        if (headers.get(Headers.ROUTE_BACK_ID) == null) {
            throw new RuntimeException("no route back id in message: " + message);
        }

        if (headers.get(Headers.ROUTE_BACK_TARGET) == null) {
            throw new RuntimeException("no route back target in message: " + message);
        }

        sendRouteBackViaLedger(message);
    }

    @Override
    public Cancelable listen(boolean replayLedger, Consumer<Message> listener) {
        if (replayLedger) {
            try {
                ledger.replayLedger(listener)
                        .await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        listeners.add(listener);

        return () -> listeners.remove(listener);
    }

    @Override
    public void cancel() {
        listeners.clear();

        channels.stream()
                .map(Channel::close)
                .collect(Collectors.toList())
                .forEach(GossipNode::sync);

        channels.clear();
    }


    private class ChannelListMaintainer extends ChannelDuplexHandler {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            channels.add(ctx.channel());
            notifyMembershipChange(joinedListeners, ctx.channel().remoteAddress());
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            channels.remove(ctx.channel());
            notifyMembershipChange(resignedListeners, ctx.channel().remoteAddress());
            super.channelInactive(ctx);
        }

        private void notifyMembershipChange(VolatileCollection<Consumer<InetSocketAddress>> listeners,
                                            SocketAddress socketAddress) {
            if (!(socketAddress instanceof InetSocketAddress)) {
                return;
            }

            listeners.forEach(listener ->
                    listener.accept((InetSocketAddress) socketAddress));
        }
    }

    private class LedgerHandler extends SimpleChannelInboundHandler<Message> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx,
                                    Message message) throws Exception {

            if (message.getHeaders().isRouteBack()) {
                sendRouteBackViaLedger(message);
                return;
            }

            ledger.sendMessage(message, ctx.channel().id());
        }
    }

    private void sendRouteBackViaLedger(Message message) {
        ledger.sendViaBackRoute(message);
    }

    private static ChannelFuture sync(ChannelFuture channelFuture) {
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return channelFuture;
    }

    public static class Pools implements Cancelable {
        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;
        EventLoopGroup clientGroup;
        ClassResolver classResolver;

        public Pools() {
            bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("server"));
            workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
            clientGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("client"));
            classResolver = softCachingConcurrentResolver(this.getClass().getClassLoader());
        }

        public void cancel() {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }

    private class ExceptionHandler extends ChannelDuplexHandler {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof IOException && "Connection reset by peer".equals(cause.getMessage())) {
                return;
            }
            log.error("Transmission error", cause);
        }
    }
}
