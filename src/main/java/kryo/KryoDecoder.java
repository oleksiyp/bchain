package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.function.Consumer;

public class KryoDecoder extends ByteToMessageDecoder {
    Kryo kryo;

    Input input = new Input(4096);

    public KryoDecoder(Consumer<Kryo> factory, KryoObjectPool pool) {
        kryo = new KryoFactory(factory, pool).get();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in,
                          List<Object> out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }

        in.markReaderIndex();

        int len = in.readUnsignedShort();

        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }

        ByteBufInputStream inStream = new ByteBufInputStream(in, len);
        input.setInputStream(inStream);
        Object object = kryo.readClassAndObject(input);
        out.add(object);
        input.setInputStream(null);
    }
}
