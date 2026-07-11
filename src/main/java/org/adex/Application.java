package org.adex;

import org.adex.backend.BackendRegistry;
import org.adex.backend.DefaultBackendRegistry;
import org.adex.config.HealthCheck;
import org.adex.config.LoadBalancerConfig;
import org.adex.config.StrategyFactory;
import org.adex.config.YamlConfigLoader;
import org.adex.health.DefaultHealthChecker;
import org.adex.health.HealthCheckScheduler;
import org.adex.health.HealthCheckService;
import org.adex.proxy.DefaultRequestForwarder;
import org.adex.proxy.DefaultReverseProxy;
import org.adex.proxy.RequestForwarder;
import org.adex.server.DefaultLoadBalancerServer;
import org.adex.server.LoadBalancerServer;
import org.adex.strategy.LoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public interface Application {

    Logger LOGGER = LoggerFactory.getLogger(Application.class);

    static void main(String... args) {
        LoadBalancerServer server = null;

        try {
            LoadBalancerConfig loadBalancerConfig = new YamlConfigLoader().load(args[0]);

            final BackendRegistry backendRegistry = new DefaultBackendRegistry();

            backendRegistry.register(loadBalancerConfig.backends());

            HealthCheck healthCheck = loadBalancerConfig.healthCheck();

            if (Objects.nonNull(healthCheck)) {
                final HealthCheckService healthCheckService = new HealthCheckService(new DefaultHealthChecker(healthCheck.path()), backendRegistry);
                final HealthCheckScheduler healthCheckScheduler = new HealthCheckScheduler(healthCheckService, healthCheck.duration());
                healthCheckScheduler.start();
            }

            LOGGER.info("Registered {} backend servers", backendRegistry.allBackends().size());

            final LoadBalancingStrategy roundRobinStrategy = StrategyFactory.create(loadBalancerConfig.strategy());

            final RequestForwarder requestForwarder = new DefaultRequestForwarder();
            final DefaultReverseProxy proxy = new DefaultReverseProxy(backendRegistry, roundRobinStrategy, requestForwarder);
            server = new DefaultLoadBalancerServer(loadBalancerConfig.port(), proxy);

            server.start();

            LOGGER.info("Load balancer started successfully on port : " + server.port());

            LoadBalancerServer runningServer = server;

            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(() -> {
                                LOGGER.info("Stopping load balancer...");
                                runningServer.stop();
                                LOGGER.info("Load balancer stoped");
                            })
                    );



        } catch (Exception e) {
            LOGGER.error("Failed to start load balancer", e);
            if (server != null && server.isRunning()) {
                server.stop();
            }
            System.exit(1);
        }

    }
}
