package org.adex.server;

public interface LoadBalancerServer {

    void start() throws Exception;

    void stop();

    boolean isRunning();

    int port();
}
