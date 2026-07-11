package org.adex.config;

public record ServerConfig(int port, LoadBalancerStrategy strategy) {
}
