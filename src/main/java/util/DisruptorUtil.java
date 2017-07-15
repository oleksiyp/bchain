package util;

import com.lmax.disruptor.EventHandler;

public class DisruptorUtil {
    public static <T> EventHandler<T> []seq(EventHandler<? super T> ...handlers) {
        EventHandler<T> handler = (event, sequence, endOfBatch) -> {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onEvent(event, sequence, endOfBatch);
            }
        };
        return new EventHandler[] {handler};
    }
}
