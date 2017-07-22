package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.Message;
import node.datagram.SocketParty;
import node.datagram.event.Event;
import node.datagram.event.WriteEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import static node.datagram.event.WriteEvent.TYPE;

@Slf4j
public class WriteHandler implements EventHandler<Event> {
    private final ByteBuffer buffer;

    public WriteHandler(int bufSize) {
        buffer = ByteBuffer.allocateDirect(bufSize);
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(TYPE)) {
            onWriteEvent(event);
        }
    }

    AtomicInteger cnt = new AtomicInteger();

    private void onWriteEvent(Event event) throws IOException, InterruptedException {
        WriteEvent writeEvent = event.getSubEvent(TYPE);

        SocketParty party = (SocketParty) writeEvent.getTo();
        party.writeQ()
                .enQ(message -> message.copyFrom(writeEvent.getMessage()));

        party.writeInterested();
//        SocketChannel channel = party.getChannel();
//
//        buffer.clear();
//        buffer.putInt(0);
//        int frameStart = buffer.position();
//        writeEvent.getMessage().serialize(buffer);
//        int frameEnd = buffer.position();
//        buffer.putInt(0, frameEnd - frameStart);
//        buffer.flip();
//
//        channel.write(buffer);
//        buffer.flip();
//        System.out.println("write " + cnt.incrementAndGet() + " " + n + " " + wasBlocking);
//
//        log.trace("Writing {} bytes of {}", buffer.remaining(), writeEvent.getMessage());
    }
}
