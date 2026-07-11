package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;
import org.adex.backend.Backend;
import org.adex.backend.BackendRegistry;
import org.adex.metrics.MetricsCollector;
import org.adex.strategy.LoadBalancingStrategy;

import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Set;

public class DefaultReverseProxy implements ReverseProxy {

    private final BackendRegistry backendRegistry;
    private final LoadBalancingStrategy strategy;
    private final RequestForwarder requestForwarder;
    private final MetricsCollector metricsCollector;

    public DefaultReverseProxy(BackendRegistry backendRegistry, LoadBalancingStrategy strategy, RequestForwarder requestForwarder, MetricsCollector metricsCollector) {
        this.backendRegistry = Objects.requireNonNull(backendRegistry);
        this.strategy = Objects.requireNonNull(strategy);
        this.requestForwarder = Objects.requireNonNull(requestForwarder);
        this.metricsCollector = Objects.requireNonNull(metricsCollector);
    }

    @Override
    public void forward(HttpExchange exchange) throws Exception {
        try {
            long start = System.currentTimeMillis();

            Set<Backend> backends = backendRegistry.onlyAvailableBackends();
            metricsCollector.recordRequest();

            if (Objects.isNull(backends) || backends.isEmpty()) {
                metricsCollector.recordFailure();
                sendError(exchange, 503, "No backend available");
                return;
            }

            Backend backend = strategy.select(backends);
            HttpResponse<byte[]> response = requestForwarder.forward(backend, exchange);

            long latency = System.currentTimeMillis() - start;
            metricsCollector.recordSuccess(latency);

            exchange.getResponseHeaders().putAll(response.headers().map());
            exchange.sendResponseHeaders(response.statusCode(), response.body().length);
            exchange.getResponseBody().write(response.body());
        } catch (Exception e) {
            metricsCollector.recordFailure();
            throw e;
        } finally {
            exchange.close();
        }
    }

    private void sendError(HttpExchange exchange, int codeStatus, String msg) throws Exception {
        byte[] body = msg.getBytes();
        exchange.sendResponseHeaders(codeStatus, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
