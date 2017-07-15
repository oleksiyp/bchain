package node.datagram.shared;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.function.BiConsumer;

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
                T event = ringBuffer.get(i + next - n + 1);
                consumer.accept(i, event);
            }
        } finally {
            ringBuffer.publish(next - n + 1,next);
        }
    }
}
