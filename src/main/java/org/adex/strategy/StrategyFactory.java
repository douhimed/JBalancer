package org.adex.strategy;

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
