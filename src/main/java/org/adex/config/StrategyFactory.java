package org.adex.config;

import org.adex.strategy.LoadBalancingStrategy;
import org.adex.strategy.RoundRobinStrategy;
import org.adex.strategy.WeightedRoundRobinStrategy;

public final class StrategyFactory {

    public StrategyFactory() {
        throw  new RuntimeException("use create(Strategy strategy)");
    }

    public static LoadBalancingStrategy create(LoadBalancerStrategy strategy) {
        return switch (strategy) {
            case ROUND_ROBIN ->  RoundRobinStrategy.getInstance();
            case WEIGHTED_ROUND_ROBIN ->  WeightedRoundRobinStrategy.getInstance();
        };
    }
}
