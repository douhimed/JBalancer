package org.adex.metrics;

public record MetricsSnapshot(
        long requests,
        long successes,
        long failures,
        double averageLatency) {

    public String capture() {
        return """
                    {
                      "requests": %d,
                      "successes": %d,
                      "failures": %d,
                      "averageLatency": %.2f
                    }
                    """.formatted(requests(), successes(), failures(), averageLatency());

    }
}
