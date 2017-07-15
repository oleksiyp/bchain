package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import node.Message;

import java.io.OutputStream;
import java.util.function.Consumer;

public class KryoEncoder extends MessageToByteEncoder<Message> {
    Kryo kryo;

    Output output = new Output(4096);

    public KryoEncoder(Consumer<Kryo> factory, KryoObjectPool pool) {
        kryo = new KryoFactory(factory, pool).get();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          Message msg,
                          ByteBuf out) throws Exception {
        out.writeShort(0);

        int startIdx = out.writerIndex();

        OutputStream outStream = new ByteBufOutputStream(out);

        output.clear();
        output.setOutputStream(outStream);
        kryo.writeClassAndObject(output, msg);
        output.flush();
        output.setOutputStream(null);

        int endIdx = out.writerIndex();

        out.setShort(0, endIdx - startIdx);
    }
}
