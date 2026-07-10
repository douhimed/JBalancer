package org.adex.strategy;

import org.adex.backend.Backend;

import java.util.Set;

public final class RoundRobinStrategy implements LoadBalancingStrategy {

    @Override
    public Backend select(Set<Backend> backends) {
        return null;
    }
}
