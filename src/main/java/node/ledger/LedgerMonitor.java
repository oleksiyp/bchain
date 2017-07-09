package node.ledger;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LedgerMonitor {
    long prevStart;
    long prevEnd;

    public LedgerMonitor(LedgerDispatcher dispatcher) {
        new Thread(() -> {
            while (true) {
                try {
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
                long start = dispatcher.getStart();
                long end = dispatcher.getEnd();
                long n = start - end;
                n += dispatcher.getQSize();

                long added = start - prevStart;
                long removed = end - prevEnd;

                System.out.println(n + " +" + added + " -" + removed);

                prevStart = start;
                prevEnd = end;
            }
        }, "ledger-monitor").start();

    }
}
