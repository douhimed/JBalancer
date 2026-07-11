package org.adex.config;

import org.adex.backend.Backend;
import org.adex.health.HealthCheck;
import org.adex.server.ServerConfig;
import org.adex.strategy.LoadBalancerStrategy;

import java.util.Set;
import java.util.stream.Collectors;

public record LoadBalancerConfig(ServerConfig server, HealthCheck healthCheck, Set<BackendConfig> services){

    public Set<Backend> backends() {
        return services
                .stream()
                .map(bc -> new Backend(bc.id(), bc.host(), bc.port(), bc.weight()))
                .collect(Collectors.toSet());
    }

    public LoadBalancerStrategy strategy() {
        return server.strategy();
    }

    public int port() {
        return server.port();
    }
}
