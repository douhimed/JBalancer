package org.adex;

import org.adex.backend.BackendRegistry;
import org.adex.backend.DefaultBackendRegistry;
import org.adex.config.ConfigLoader;
import org.adex.config.LoadBalancerConfig;
import org.adex.health.HealthCheck;
import org.adex.health.HealthCheckScheduler;
import org.adex.health.HealthCheckService;
import org.adex.health.DefaultHealthChecker;
import org.adex.metrics.DefaultMetricsCollector;
import org.adex.metrics.MetricsCollector;
import org.adex.proxy.DefaultReverseProxy;
import org.adex.resilience.CircuitBreaker;
import org.adex.resilience.CircuitBreakerRegistry;
import org.adex.resilience.DefaultCircuitBreakerRegistry;
import org.adex.server.DefaultLoadBalancerServer;
import org.adex.server.LoadBalancerServer;
import org.adex.strategy.LoadBalancingStrategy;
import org.adex.strategy.StrategyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.Duration;
import java.util.Objects;


public interface Application {


    Logger LOGGER = LoggerFactory.getLogger(Application.class);


    static void main(String... args) {

        LoadBalancerServer server = null;

        try {
            /*
             * ============================
             * Validate arguments
             * ============================
             */
            if (args.length == 0) {
                throw new IllegalArgumentException("Missing configuration file path");
            }

            /*
             * ============================
             * Load configuration
             * ============================
             */
            LoadBalancerConfig config = ConfigLoader.from(args[0]);

            /*
             * ============================
             * Backend registry
             * ============================
             */
            BackendRegistry backendRegistry = new DefaultBackendRegistry();
            backendRegistry.register(config.backends());
            LOGGER.info("Registered {} backend servers", backendRegistry.allBackends().size());

            /*
             * ============================
             * Circuit breaker registry
             * ============================
             */
            CircuitBreakerRegistry circuitBreakerRegistry = new DefaultCircuitBreakerRegistry(3, Duration.ofSeconds(30));
            circuitBreakerRegistry.register(backendRegistry.allBackends());
            LOGGER.info("Registered {} circuit breakers", circuitBreakerRegistry.size());

            /*
             * ============================
             * Circuit breaker test mode
             * ============================
             */
            if (hasTestArgument(args)) {
                testCircuitBreaker(circuitBreakerRegistry);
                //return;
            }

            /*
             * ============================
             * Health check
             * ============================
             */
            HealthCheck healthCheck = config.healthCheck();
            if (Objects.nonNull(healthCheck)) {
                HealthCheckService healthCheckService = new HealthCheckService(new DefaultHealthChecker(healthCheck.path()), backendRegistry);
                HealthCheckScheduler scheduler = new HealthCheckScheduler(healthCheckService, healthCheck.duration());
                scheduler.start();
                LOGGER.info("Health check scheduler started");
            }

            /*
             * ============================
             * Load balancing strategy
             * ============================
             */
            LoadBalancingStrategy strategy = StrategyFactory.create(config.strategy());

            /*
             * ============================
             * Metrics
             * ============================
             */
            MetricsCollector metricsCollector = new DefaultMetricsCollector();

            /*
             * ============================
             * Reverse proxy
             * ============================
             */
            DefaultReverseProxy proxy = new DefaultReverseProxy(backendRegistry, strategy, metricsCollector, circuitBreakerRegistry);

            /*
             * ============================
             * HTTP server
             * ============================
             */
            server = new DefaultLoadBalancerServer(config.port(), proxy, metricsCollector);

            server.start();
            LOGGER.info("Load balancer started successfully on port {}", server.port());

            LoadBalancerServer runningServer = server;
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(
                                    () -> {
                                        LOGGER.info("Stopping load balancer...");
                                        runningServer.stop();
                                        LOGGER.info("Load balancer stopped");
                                    }));
        } catch (Exception e) {
            LOGGER.error("Failed to start load balancer", e);

            if (server != null && server.isRunning()) {
                server.stop();
            }
            System.exit(1);
        }
    }

    private static boolean hasTestArgument(String[] args) {
        for (String arg : args) {
            if ("--test-circuit-breaker".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void testCircuitBreaker(CircuitBreakerRegistry registry) {

        LOGGER.info("========== Circuit breaker test ==========");
        CircuitBreaker breaker = registry.get("backend-1");

        /*
         * Initial state
         */
        LOGGER.info("Initial request allowed: {}", breaker.allow());

        /*
         * Simulate failures
         */
        LOGGER.info("Simulating 3 failures...");

        breaker.failure();
        breaker.failure();
        breaker.failure();

        LOGGER.info("Request allowed after failures: {}", breaker.allow());

        /*
         * Wait for recovery
         */
        LOGGER.info("Waiting for reset timeout...");
        try {
            Thread.sleep(Duration.ofSeconds(31));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        LOGGER.info("Request allowed after timeout: {}", breaker.allow());

        /*
         * Simulate recovery
         */
        LOGGER.info("Simulating successful request");

        breaker.success();
        LOGGER.info("Request allowed after success: {}", breaker.allow());

        LOGGER.info("========== Test finished ==========");
    }

}