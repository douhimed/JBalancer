package org.adex.strategy;

import org.adex.backend.Backend;

import java.util.Set;

public sealed interface LoadBalancingStrategy permits RoundRobinStrategy, WeightedRoundRobinStrategy {

    Backend select(Set<Backend> backends);
}
