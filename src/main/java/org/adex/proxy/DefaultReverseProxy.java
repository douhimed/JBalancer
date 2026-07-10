package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;
import org.adex.backend.Backend;
import org.adex.backend.BackendRegistry;
import org.adex.strategy.LoadBalancingStrategy;

import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Set;

public class DefaultReverseProxy implements ReverseProxy {

    private final BackendRegistry backendRegistry;
    private final LoadBalancingStrategy strategy;
    private final RequestForwarder requestForwarder;

    public DefaultReverseProxy(BackendRegistry backendRegistry, LoadBalancingStrategy strategy, RequestForwarder requestForwarder) {
        this.backendRegistry = Objects.requireNonNull(backendRegistry);
        this.strategy = Objects.requireNonNull(strategy);
        this.requestForwarder = Objects.requireNonNull(requestForwarder);
    }

    @Override
    public void forward(HttpExchange exchange) throws Exception {

        Set<Backend> backends = backendRegistry.onlyAvailableBackends();

        if (Objects.isNull(backends) || backends.isEmpty()) {
            sendError(exchange, 503, "No backend available");
            return;
        }

        Backend backend = strategy.select(backends);
        HttpResponse<byte[]> response = requestForwarder.forward(backend, exchange);
        exchange.getResponseHeaders().putAll(response.headers().map());
        exchange.sendResponseHeaders(response.statusCode(), response.body().length);
        exchange.getResponseBody().write(response.body());
        exchange.close();
    }

    private void sendError(HttpExchange exchange, int codeStatus, String msg) throws Exception {
        byte[] body = msg.getBytes();
        exchange.sendResponseHeaders(codeStatus, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
