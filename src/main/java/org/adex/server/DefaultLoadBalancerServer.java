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

public class DefaultLoadBalancerServer implements LoadBalancerServer {

    private final int port;
    private final ReverseProxy proxy;
    private HttpServer server;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MetricsCollector metricsCollector;

    public DefaultLoadBalancerServer(int port, ReverseProxy proxy, MetricsCollector metricsCollector) {
        this.port = port;
        this.proxy = Objects.requireNonNull(proxy, "reverse proxy cannot be null");
        this.metricsCollector = Objects.requireNonNull(metricsCollector, "metrics collector cannot be null");
    }

    @Override
    public void start() throws Exception {

        if (running.get()) {
            throw new IllegalStateException("Server is already stopped");
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            setRootContext();
            setMetricsContext();
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            running.set(true);
        } catch (IOException e) {
            throw new Exception("Failed to start load balancer on port " + port, e);
        }

    }

    private void setMetricsContext() {
        server.createContext("/metrics", new MetricsHandler(metricsCollector));
    }

    private void setRootContext() {
        server.createContext("/", exchange -> {
            try {
                proxy.forward(exchange);
            } catch (Exception e) {
                e.printStackTrace();

                byte[] res = "Internal Server Error".getBytes();
                exchange.sendResponseHeaders(500, res.length);
                exchange.getResponseBody().write(res);
            } finally {
                exchange.close();
            }
        });
    }

    @Override
    public synchronized void stop() {
        if (!running.get()) {
            return;
        }

        if (server != null) {
            server.stop(0);
        }

        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int port() {
        return port;
    }
}
