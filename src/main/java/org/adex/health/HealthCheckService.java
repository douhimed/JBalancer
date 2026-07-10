package org.adex.health;

import org.adex.backend.Backend;
import org.adex.backend.BackendRegistry;
import org.adex.backend.BackendStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);

    private final HealthChecker healthChecker;
    private final BackendRegistry registry;

    public HealthCheckService(HealthChecker healthChecker, BackendRegistry registry) {
        this.healthChecker = healthChecker;
        this.registry = registry;
    }

    public void check(Backend backend) {
        boolean healthy = healthChecker.check(backend);
        LOGGER.info("Health check status: " + backend.url() + " is " + (healthy ? BackendStatus.UP : BackendStatus.DOWN));
        registry.upateStatus(backend.id(), healthy ? BackendStatus.UP : BackendStatus.DOWN);
    }

    public void checkAll() {
        registry.allBackends().forEach(this::check);
    }
}
