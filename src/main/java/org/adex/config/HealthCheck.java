package org.adex.config;

public record HealthCheck(boolean enabled, int duration, String path) {
}
