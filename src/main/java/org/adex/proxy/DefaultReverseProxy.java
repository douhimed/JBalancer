package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;

import org.adex.backend.Backend;
import org.adex.backend.BackendRegistry;
import org.adex.metrics.MetricsCollector;
import org.adex.resilience.CircuitBreaker;
import org.adex.resilience.CircuitBreakerRegistry;
import org.adex.server.DefaultResponseWriter;
import org.adex.server.ResponseWriter;
import org.adex.strategy.LoadBalancingStrategy;


import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Set;


public class DefaultReverseProxy implements ReverseProxy {


    private final BackendRegistry backendRegistry;

    private final LoadBalancingStrategy strategy;

    private RequestForwarder requestForwarder;

    private ResponseWriter responseWriter;

    private final MetricsCollector metricsCollector;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public DefaultReverseProxy(
            BackendRegistry backendRegistry,
            LoadBalancingStrategy strategy,
            MetricsCollector metricsCollector,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        this.backendRegistry = Objects.requireNonNull(backendRegistry);
        this.strategy = Objects.requireNonNull(strategy);
        this.metricsCollector = Objects.requireNonNull(metricsCollector);
        this.circuitBreakerRegistry = Objects.requireNonNull(circuitBreakerRegistry);
        this.requestForwarder = new DefaultRequestForwarder();
        this.responseWriter = new DefaultResponseWriter();
    }

    @Override
    public void forward(HttpExchange exchange) {
        metricsCollector.recordRequest();
        long start = System.nanoTime();
        CircuitBreaker breaker = null;
        try {
            Backend backend = selectBackend();
            if (backend == null) {
                metricsCollector.recordFailure();
                responseWriter.write(exchange, 503, "No backend available");
                return;
            }
            breaker = circuitBreakerRegistry.get(backend.id());
            if (!breaker.allow()) {
                metricsCollector.recordFailure();
                responseWriter.write(exchange, 503, "Backend temporarily unavailable");
                return;
            }

            HttpResponse<byte[]> response = requestForwarder.forward(backend, exchange);
            breaker.success();
            metricsCollector.recordSuccess(latency(start));
            responseWriter.write(exchange, response);
        } catch (ConnectException e) {
            backendFailure(breaker);
            safeError(exchange, 502, "Backend connection failed");
        } catch (HttpTimeoutException e) {
            backendFailure(breaker);
            safeError(exchange, 504, "Backend timeout");
        } catch (Exception e) {
            metricsCollector.recordFailure();
            safeError(exchange,500,"Internal proxy error");
        } finally {
            exchange.close();
        }

    }

    private Backend selectBackend() {
        Set<Backend> backends = backendRegistry.onlyAvailableBackends();
        if (backends.isEmpty()) {
            return null;
        }
        return strategy.select(backends);
    }

    private void backendFailure(CircuitBreaker breaker) {
        metricsCollector.recordFailure();
        if (breaker != null) {
            breaker.failure();
        }
    }

    private void safeError(HttpExchange exchange, int status, String message) {
        try {
            responseWriter.write(exchange, status, message);
        } catch (Exception ignored) {
        }
    }

    private long latency(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    public void withRequestForwarder(RequestForwarder requestForwarder) {
        this.requestForwarder = Objects.requireNonNull(requestForwarder);
    }

    public void withResponseWriter(ResponseWriter responseWriter) {
        this.responseWriter = Objects.requireNonNull(responseWriter);
    }
}