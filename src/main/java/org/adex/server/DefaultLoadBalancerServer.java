package org.adex.server;

public class DefaultLoadBalancerServer implements LoadBalancerServer {

    private final int port;

    public DefaultLoadBalancerServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
