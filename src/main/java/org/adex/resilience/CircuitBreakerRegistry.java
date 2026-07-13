package org.adex.resilience;

import org.adex.backend.Backend;

import java.util.Collection;

public interface CircuitBreakerRegistry {

    void register(Backend backend);

    void register(Collection<Backend> backends);

    CircuitBreaker get(String backendId);

    void remove(String backendId);

    void clear();

    int size();
}
