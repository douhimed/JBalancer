package org.adex.server;

import org.adex.strategy.LoadBalancerStrategy;

public record ServerConfig(int port, LoadBalancerStrategy strategy) {
}
