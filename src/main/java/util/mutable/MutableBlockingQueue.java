package util.mutable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MutableBlockingQueue<T> {
    private List<T> items;
    private int putIndex;
    private int takeIndex;
    private int count;

    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;


    public MutableBlockingQueue(Supplier<T> factory, int size) {
        items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(factory.get());
        }
        putIndex = takeIndex = 0;

        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    public void enQ(Consumer<T> consumer) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == items.size()) {
                notFull.await();
            }
            T ret = items.get(putIndex);
            consumer.accept(ret);
            if (++putIndex == items.size()) {
                putIndex = 0;
            }
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }


    public void deQ(boolean ifPresent, Consumer<T> consumer) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            if (count == 0 && ifPresent) {
                return;
            }
            while (count == 0) {
                notEmpty.await();
            }
            T ret = items.get(takeIndex);
            consumer.accept(ret);
            if (++takeIndex == items.size()) {
                takeIndex = 0;
            }
            count--;
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return count == 0;
        } finally {
            lock.unlock();
        }
    }
}
