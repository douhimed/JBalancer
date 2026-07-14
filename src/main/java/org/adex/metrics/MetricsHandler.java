package org.adex.metrics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.adex.server.DefaultResponseWriter;
import org.adex.server.ResponseWriter;

import java.io.IOException;

public class MetricsHandler implements HttpHandler {

    private final MetricsCollector metricsCollector;
    private ResponseWriter  responseWriter;

    public MetricsHandler(MetricsCollector metricsCollector) {
        this(metricsCollector, new DefaultResponseWriter());
    }

    public MetricsHandler(MetricsCollector metricsCollector, ResponseWriter responseWriter) {
        this.metricsCollector = metricsCollector;
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            MetricsSnapshot snapshot = metricsCollector.snapshot();
            String response = snapshot.capture();
            responseWriter.write(exchange, 200, response);
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
