package org.adex.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultCircuitBreaker implements CircuitBreaker{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCircuitBreaker.class);

    private final int failureThreshold;
    private final Duration resetTimeout;

    private volatile long openedAt;

    private final AtomicInteger failure =  new AtomicInteger();
    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<>(CircuitBreakerState.CLOSED);

    public DefaultCircuitBreaker(int failureThreshold, Duration resetTimeout) {
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;

        LOGGER.info("Circuit breaker created threshold={}, timeout={}", failureThreshold, resetTimeout);
    }

    @Override
    public boolean allow() {

        CircuitBreakerState current = state.get();

        LOGGER.debug("Circuit breaker check state={}, failures={}", current, failure.get());

        if (current.isClosed()) {
            return true;
        }

        if (current.isHalfOpen()) {
            return true;
        }

        long elapsed = System.currentTimeMillis() - openedAt;

        if (elapsed < resetTimeout.toMillis()) {
            return false;
        }

        boolean changed = state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN);

        if (changed) {
            LOGGER.info("Circuit breaker moved OPEN -> HALF_OPEN");
        }

        return true;
    }

    @Override
    public void success() {
        LOGGER.info("Request successful, closing circuit breaker");
        failure.set(0);
        state.set(CircuitBreakerState.CLOSED);
    }

    @Override
    public void failure() {
        CircuitBreakerState current = state.get();

        if(current.isHalfOpen()) {
            open();
            return;
        }

        int count = failure.incrementAndGet();

        LOGGER.warn("Request failed count={}/{}", count, failureThreshold);

        if (count >= failureThreshold) {
            open();
            LOGGER.error("Circuit breaker OPEN after {} failures", count);
        }
    }

    private void open() {
        state.set(CircuitBreakerState.OPEN);
        openedAt = System.currentTimeMillis();
    }

    public CircuitBreakerState state() {
        return state.get();
    }

    public int failures() {
        return failure.get();
    }
}
