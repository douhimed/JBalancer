package org.adex.server;

import com.sun.net.httpserver.HttpServer;
import org.adex.metrics.MetricsCollector;
import org.adex.metrics.MetricsHandler;
import org.adex.proxy.ReverseProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLoadBalancerServer implements LoadBalancerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLoadBalancerServer.class);

    private static final String ROOT_CONTEXT = "/";
    private static final String METRICS_CONTEXT = "/metrics";

    private final int port;
    private final ReverseProxy reverseProxy;
    private final MetricsCollector metricsCollector;
    private final ResponseWriter responseWriter;
    private final AtomicBoolean running = new AtomicBoolean();

    private HttpServer server;

    public DefaultLoadBalancerServer(int port, ReverseProxy reverseProxy, MetricsCollector metricsCollector) {
        this(port, reverseProxy, metricsCollector, new DefaultResponseWriter());
    }

    public DefaultLoadBalancerServer(int port, ReverseProxy reverseProxy, MetricsCollector metricsCollector, ResponseWriter responseWriter) {
        this.port = port;
        this.reverseProxy = Objects.requireNonNull(reverseProxy);
        this.metricsCollector = Objects.requireNonNull(metricsCollector);
        this.responseWriter = Objects.requireNonNull(responseWriter);
    }

    @Override
    public synchronized void start() throws IOException {

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Load balancer is already running");
        }

        try {
            server = createServer();
            registerContexts();
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            LOGGER.info("HTTP server started on port {}", port);
        } catch (IOException e) {
            running.set(false);
            throw e;
        }
    }

    @Override
    public synchronized void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (server != null) {
            LOGGER.info("Stopping HTTP server...");
            server.stop(0);
            LOGGER.info("HTTP server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int port() {
        return port;
    }

    private HttpServer createServer() throws IOException {
        return HttpServer.create(new InetSocketAddress(port), 0);
    }

    private void registerContexts() {
        registerProxyContext();
        registerMetricsContext();
    }

    private void registerProxyContext() {
        server.createContext(ROOT_CONTEXT, exchange -> {
            try {
                reverseProxy.forward(exchange);
            } catch (Exception e) {
                LOGGER.error("Failed to process request {} {}", exchange.getRequestMethod(), exchange.getRequestURI(), e);
                try {
                    responseWriter.write(exchange, 500, "Internal Server Error");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void registerMetricsContext() {
        server.createContext(METRICS_CONTEXT, new MetricsHandler(metricsCollector));
    }
}