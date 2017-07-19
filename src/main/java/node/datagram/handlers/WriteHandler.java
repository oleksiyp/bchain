package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import node.datagram.event.Event;
import node.datagram.event.WriteEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

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

    private void onWriteEvent(Event event) throws IOException {
        WriteEvent writeEvent = event.getSubEvent(TYPE);
        buffer.clear();
        writeEvent.getMessage().serialize(buffer);
        buffer.flip();
        DatagramChannel channel = writeEvent.getTo().getChannel();
        channel.write(buffer);
        buffer.flip();
        log.trace("Writing {} bytes of {}", buffer.remaining(), writeEvent.getMessage());
    }
}
