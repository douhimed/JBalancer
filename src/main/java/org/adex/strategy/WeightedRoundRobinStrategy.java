package org.adex.strategy;

import org.adex.backend.Backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class WeightedRoundRobinStrategy implements LoadBalancingStrategy {

    private final AtomicInteger counter = new AtomicInteger(0);

    private static WeightedRoundRobinStrategy instance;

    public WeightedRoundRobinStrategy() {
        if (Objects.nonNull(instance)) {
            throw new IllegalStateException("WeightedRoundRobinStrategy has already been initialized, use getInstance() instead.");
        }
    }

    public static WeightedRoundRobinStrategy getInstance() {
        if (Objects.isNull(instance)) {
            instance = new WeightedRoundRobinStrategy();
        }
        return instance;
    }

    @Override
    public Backend select(Set<Backend> backends) {

        if (Objects.isNull(backends) || backends.isEmpty()) {
            throw new IllegalArgumentException("No backend available");
        }

        List<Backend> servers = buildWeightedServers(backends);

        int index = Math.abs(counter.getAndIncrement()) % servers.size();
        return servers.get(index);
    }

    private List<Backend> buildWeightedServers(Set<Backend> backends) {
        List<Backend> servers = new ArrayList<>();

        for(Backend backend : backends) {
            int weight = Math.max(backend.weight(), 1);
            for(int i = 0; i < weight; i++) {
                servers.add(backend);
            }
        }

        return servers;
    }
}
