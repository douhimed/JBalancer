package org.adex.config;

public record BackendConfig(String id, String host, int port, int weight) {
}
