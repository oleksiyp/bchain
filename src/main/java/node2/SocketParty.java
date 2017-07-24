package node2;

import lombok.Getter;
import lombok.ToString;
import node2.in_out.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.OP_WRITE;

public class SocketParty {
    @Getter
    private SocketGossip gossip;

    private SocketChannel channel;

    private final SelectionKey key;

    private int ops;

    private int maxInMsgSize;
    private int skipInNextNBytes;
    private ByteBuffer inBuffer;
    private ByteBuffer deserializeBuf;
    private In in;

    private ByteBuffer outBuffer;
    private CountOut countOut;
    private Out out;


    public SocketParty(SocketGossip gossip,
                       SocketChannel channel,
                       SelectionKey key,
                       int maxInMsgSize) {
        this.gossip = gossip;
        this.channel = channel;
        this.key = key;
        this.maxInMsgSize = maxInMsgSize;

        inBuffer = ByteBuffer.allocate(64 * 1024);
        deserializeBuf = inBuffer.duplicate();
        in = new BufIn(deserializeBuf);

        outBuffer = ByteBuffer.allocate(512);
        countOut = new CountOut();
        out = new BufOut(outBuffer);
        ops = key.interestOps();
    }

    public Message receive() throws IOException {
        while (true) {
            int bufSize = inBuffer.position();

            int n = Math.min(bufSize, skipInNextNBytes);
            if (n > 0) {
                inBuffer.position(n).limit(bufSize);
                inBuffer.compact();
                skipInNextNBytes -= n;
                continue;
            }

            int frameStart = 4;

            if (frameStart > bufSize) {
                channel.read(inBuffer);
                bufSize = inBuffer.position();
                if (frameStart > bufSize) {
                    return null;
                }
            }

            int frameEnd = inBuffer.getInt(0) + frameStart;

            if (frameEnd >= maxInMsgSize) {
                skipInNextNBytes = frameEnd;
                continue;
            } else if (frameEnd > inBuffer.capacity()) {
                reallocateIn(frameEnd);
                continue;
            } else if (frameEnd > bufSize) {
                channel.read(inBuffer);
                bufSize = inBuffer.position();
                if (frameEnd > bufSize) {
                    return null;
                }
            }

            deserializeBuf.position(frameStart).limit(frameEnd);
            inBuffer.position(frameEnd).limit(bufSize);

            try {
                int tag = deserializeBuf.getInt();
                RegistryMapping<MessageType<Message>, Message> typeMapping = gossip.getShared().getTypeMapping();

                int idx = typeMapping.idxByTag(tag);
                Supplier<Message> supplier = typeMapping.constructorByIdx(idx);

                Message msg = supplier.get();

                msg.deserialize(in);

                return msg;
            } finally {
                inBuffer.compact();
            }
        }
    }

    private void reallocateIn(int bufSz) {
        ByteBuffer prevBuf = inBuffer;
        int newSize = inBuffer.capacity() * 2;
        while (newSize < bufSz) {
            newSize *= 2;
        }
        inBuffer = ByteBuffer.allocate(newSize);
        deserializeBuf = inBuffer.duplicate();
        in = new BufIn(deserializeBuf);
        prevBuf.flip();
        inBuffer.put(prevBuf);
    }

    public void writeBuf() throws IOException {
        outBuffer.flip();
        channel.write(outBuffer);
        if (outBuffer.remaining() == 0) {
            interested(ops & ~OP_WRITE);
        }
        outBuffer.compact();
    }

    public void interested(int newOps) {
        if (ops != newOps) {
            ops = newOps;
            key.interestOps(ops);
        }
    }

    public void send(Message msg) {
        countOut.reset();
        msg.serialize(countOut);
        int requiredSpace = countOut.getSize() + Integer.BYTES * 2;
        reallocateOut(requiredSpace);

        int sizePos = outBuffer.position();
        outBuffer.putInt(0);
        int frameStart = outBuffer.position();
        outBuffer.putInt(getGossip()
                .getShared()
                .getTypeMapping()
                .tagByChoiceType(msg.getType()));
        msg.serialize(out);
        int frameEnd = outBuffer.position();

        outBuffer.putInt(sizePos, frameEnd - frameStart);

        interested(ops | OP_WRITE);
    }

    private void reallocateOut(int requiredSpace) {
        if (outBuffer.remaining() >= requiredSpace) {
            return;
        }
        ByteBuffer prevBuf = outBuffer;
        int newSize = outBuffer.capacity() * 2;
        while (newSize - outBuffer.position() < requiredSpace) {
            newSize *= 2;
        }
        outBuffer = ByteBuffer.allocate(newSize);
        out = new BufOut(outBuffer);
        prevBuf.flip();
        outBuffer.put(prevBuf);
    }
}
