package node.datagram.handlers;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
public class DisruptorDispatcher<T> implements Dispatcher<T> {
    private final RingBuffer<T> ringBuffer;

    public DisruptorDispatcher(Disruptor<T> disruptor) {
        ringBuffer = disruptor.getRingBuffer();
    }

    @Override
    public void dispatch(int n, BiConsumer<Integer, T> consumer) {
        if (n == 0) {
            return;
        }
        long next = ringBuffer.next(n);
        try {
            for (int i = 0; i < n; i++) {
                long seq = i + next - n + 1;
                T event = ringBuffer.get(seq);
                consumer.accept(i, event);
                log.trace("Dispatched {} {}", seq, event);
            }
        } finally {
            ringBuffer.publish(next - n + 1,next);
        }
    }
}
