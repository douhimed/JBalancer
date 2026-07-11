package org.adex.health;

public record HealthCheck(boolean enabled, int duration, String path) {
}
