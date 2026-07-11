package org.adex.metrics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MetricsHandler implements HttpHandler {

    private final MetricsCollector metricsCollector;

    public MetricsHandler(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            MetricsSnapshot snapshot = metricsCollector.snapshot();

            String response = snapshot.capture();

            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        } catch (Exception e) {
            try {
                exchange.sendResponseHeaders(500, 0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            exchange.close();
        }
    }
}
