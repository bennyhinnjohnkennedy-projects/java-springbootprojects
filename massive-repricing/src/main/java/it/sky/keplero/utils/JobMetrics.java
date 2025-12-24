package it.sky.keplero.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class JobMetrics {
    private final AtomicInteger processed = new AtomicInteger(0);
    private final AtomicInteger succeeded = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);

    public void incrementProcessed() { processed.incrementAndGet(); }
    public void incrementSucceeded() { succeeded.incrementAndGet(); }
    public void incrementFailed() { failed.incrementAndGet(); }

    public int getProcessed() { return processed.get(); }
    public int getSucceeded() { return succeeded.get(); }
    public int getFailed() { return failed.get(); }

    public void reset() {
        processed.set(0);
        succeeded.set(0);
        failed.set(0);
    }
}
