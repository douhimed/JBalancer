package org.adex.strategy;

import org.adex.backend.Backend;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class RoundRobinStrategy implements LoadBalancingStrategy {

    private final AtomicInteger counter = new AtomicInteger(0);

    private static RoundRobinStrategy instance;

    public RoundRobinStrategy() {
        if (Objects.nonNull(instance)) {
            throw new IllegalStateException("Strategy already initialized, use getInstance() instead");
        }
    }

    public static RoundRobinStrategy getInstance() {
        if (Objects.isNull(instance)) {
            instance = new RoundRobinStrategy();
        }
        return instance;
    }

    @Override
    public Backend select(Set<Backend> backends) {

        if (Objects.isNull(backends) || backends.isEmpty()) {
            throw new IllegalArgumentException("No backend available");
        }

        List<Backend> servers = new ArrayList<>(backends);
        int index = Math.abs(counter.getAndIncrement() % servers.size());

        return servers.get(index);
    }
}
