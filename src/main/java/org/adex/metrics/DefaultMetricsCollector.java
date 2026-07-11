package org.adex.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultMetricsCollector implements MetricsCollector {

    private final AtomicLong requests = new AtomicLong();
    private final AtomicLong successes = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private final AtomicLong totalLatency = new AtomicLong();

    @Override
    public void recordRequest() {
        requests.incrementAndGet();
    }

    @Override
    public void recordSuccess(long latency) {
        successes.incrementAndGet();
        totalLatency.addAndGet(latency);
    }

    @Override
    public void recordFailure() {
        failures.incrementAndGet();
    }

    @Override
    public MetricsSnapshot snapshot() {
        long successCount = successes.get();
        double averageLatency = successCount == 0
                ? 0
                : (double) totalLatency.get() / successCount;

        return new MetricsSnapshot(requests.get(), successCount, failures.get(), averageLatency);
    }
}
