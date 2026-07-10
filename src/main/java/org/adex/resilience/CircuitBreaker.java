package org.adex.resilience;

public interface CircuitBreaker {

    boolean allow();
    void success();
    void failure();
}
