package org.adex.metrics;

public record MetricsSnapshot(
        long requests,
        long successes,
        long failures,
        double averageLatency) {
}
