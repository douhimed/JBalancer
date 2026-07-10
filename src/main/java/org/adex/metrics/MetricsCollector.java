package org.adex.metrics;

public interface MetricsCollector {

    void recordRequest();

    void recordSuccess(long latency);

    void recordFailure();

    MetricsSnapshot snapshot();

}
