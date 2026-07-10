package org.adex.config;

import org.adex.backend.Backend;

public record LoadBalancerConfig(int port, Backend backend, String strategy){ }
