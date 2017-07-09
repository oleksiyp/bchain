package node.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.util.concurrent.DefaultThreadFactory;
import util.Cancelable;

import static io.netty.handler.codec.serialization.ClassResolvers.softCachingConcurrentResolver;

public class NettyPools implements Cancelable {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    EventLoopGroup clientGroup;
    ClassResolver classResolver;

    public NettyPools() {
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
