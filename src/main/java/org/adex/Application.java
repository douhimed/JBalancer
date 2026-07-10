package org.adex;

import org.adex.backend.Backend;
import org.adex.backend.BackendRegistry;
import org.adex.backend.DefaultBackendRegistry;
import org.adex.health.DefaultHealthChecker;
import org.adex.health.HealthCheckService;
import org.adex.proxy.DefaultRequestForwarder;
import org.adex.proxy.DefaultReverseProxy;
import org.adex.proxy.RequestForwarder;
import org.adex.server.DefaultLoadBalancerServer;
import org.adex.server.LoadBalancerServer;
import org.adex.strategy.RoundRobinStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Application {

    Logger LOGGER = LoggerFactory.getLogger(Application.class);

    static void main() {


        LoadBalancerServer server = null;

        try {

            BackendRegistry backendRegistry = new DefaultBackendRegistry();

            Backend backend1 = new Backend("backend-1", "localhost", 9001, 1);
            Backend backend2 = new Backend("backend-2", "localhost", 9002, 2);
            Backend backend3 = new Backend("backend-3", "localhost", 9003, 1);
            Backend backend4 = new Backend("backend-4", "localhost", 9004, 1);

            backendRegistry.register(backend1).register(backend2).register(backend3).register(backend4);

            DefaultHealthChecker healthChecker = new DefaultHealthChecker();
            HealthCheckService healthCheckService = new HealthCheckService(healthChecker, backendRegistry);
            healthCheckService.checkAll();

            LOGGER.info("Registered {} backend servers", backendRegistry.allBackends().size());

            final RoundRobinStrategy roundRobinStrategy = new RoundRobinStrategy();
            final RequestForwarder requestForwarder = new DefaultRequestForwarder();
            final DefaultReverseProxy proxy = new DefaultReverseProxy(backendRegistry, roundRobinStrategy, requestForwarder);
            server = new DefaultLoadBalancerServer(8080, proxy);

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
