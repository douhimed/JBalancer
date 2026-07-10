package org.adex;

import org.adex.server.LoadBalancerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Application {

    static void main() {

        final Logger LOGGER = LoggerFactory.getLogger(Application.class);

        try {
            LoadBalancerServer server = null;
            server.start();
            LOGGER.info("Load balancer started successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to start load balancer", e);
            System.exit(1);
        }

    }
}
