package org.adex.resilience;

import org.adex.backend.Backend;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCircuitBreakerRegistry implements  CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    private final int failureThreshold;

    private final Duration resetTimeout;

    public DefaultCircuitBreakerRegistry(int failureThreshold, Duration resetTimeout) {
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
    }

    @Override
    public void register(Backend backend) {
        Objects.requireNonNull(backend);
        breakers.putIfAbsent(backend.id(), new DefaultCircuitBreaker(failureThreshold, resetTimeout));
    }

    @Override
    public void register(Collection<Backend> backends) {
        Objects.requireNonNull(backends);
        backends.forEach(this::register);
    }

    @Override
    public CircuitBreaker get(String backendId) {
        CircuitBreaker breaker = breakers.get(backendId);

        if (breaker == null) {
            throw new IllegalArgumentException("No circuit breaker registered for backend: " + backendId);
        }

        return breaker;
    }

    @Override
    public void remove(String backendId) {
        breakers.remove(backendId);
    }

    @Override
    public void clear() {
        breakers.clear();
    }

    @Override
    public int size() {
        return breakers.size();
    }
}
