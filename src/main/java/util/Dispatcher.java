package util;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class Dispatcher<T extends Copyable> {
    protected final Disruptor<T> disruptor;
    protected final ConcurrentLinkedQueue<T> queue;
    protected EventFactory<T> eventFactory;

    public Dispatcher(EventFactory<T> eventFactory,
                      int ringBufferSize,
                      ThreadFactory threadFactory) {
        
        this.eventFactory = eventFactory;
        
        disruptor = new Disruptor<>(eventFactory,
                ringBufferSize, threadFactory);

        queue = new ConcurrentLinkedQueue<>();
    }


    public void start() {
        disruptor.start();
    }

    public void dispatch(Consumer<T> initializer) {
        RingBuffer<T> ringBuffer = disruptor.getRingBuffer();
        Runnable addQ = () -> {
            T event = eventFactory.newInstance();
            initializer.accept(event);
            queue.add(event);
        };

        while (!queue.isEmpty()) {
            T fromQ = queue.peek();
            if (!ringBuffer.hasAvailableCapacity(1)) {
                addQ.run();
                return;
            }
            long n;
            try {
                n = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {
                addQ.run();
                return;
            }

            try {
                fromQ.copyTo(ringBuffer.get(n));
            } finally {
                ringBuffer.publish(n);
                queue.poll();
            }
        }

        if (!ringBuffer.hasAvailableCapacity(1)) {
            addQ.run();
            return;
        }
        long n;
        try {
            n = ringBuffer.tryNext();
        } catch (InsufficientCapacityException e) {
            addQ.run();
            return;
        }

        try {
            initializer.accept(ringBuffer.get(n));
        } finally {
            ringBuffer.publish(n);
        }
    }
}
