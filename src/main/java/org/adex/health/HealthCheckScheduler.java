package org.adex.health;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;
    private final int duration;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public HealthCheckScheduler(HealthCheckService healthCheckService, int duration) {
        this.healthCheckService = healthCheckService;
        this.duration = duration;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(healthCheckService::checkAll, 0, duration, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

}
