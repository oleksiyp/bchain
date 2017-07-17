package node.datagram.handlers;

import com.lmax.disruptor.EventHandler;
import node.datagram.event.Event;
import node.datagram.event.WriteEvent;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static node.datagram.event.EventType.WRITE_EVENT;

public class WriteHandler implements EventHandler<Event> {
    private final ByteBuffer buffer;

    public WriteHandler(int bufSize) {
        buffer = ByteBuffer.allocateDirect(bufSize);
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isSubEventActive(WRITE_EVENT)) {
            onWriteEvent(event);
        }
    }

    private void onWriteEvent(Event event) throws IOException {
        WriteEvent writeEvent = event.getSubEvent(WRITE_EVENT);
        buffer.clear();
        writeEvent.getMessage().serialize(buffer);
        buffer.flip();
        DatagramChannel channel = writeEvent.getParty().getChannel();
        SocketAddress sendAddress = writeEvent.getParty().getAddress().toInetSocketAddress();
        channel.send(buffer, sendAddress);
        writeEvent.getMessage().clear();
    }
}
