package org.adex.server;

public interface LoadBalancerServer {

    void start() throws Exception;

    void stop() throws Exception;

    boolean isRunning();
}
