package org.adex.resilience;

public enum CircuitBreakerState {
    CLOSED, OPEN, HALF_OPEN;

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isHalfOpen() {
        return this == HALF_OPEN;
    }
}
