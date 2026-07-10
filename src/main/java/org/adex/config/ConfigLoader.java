package org.adex.config;

public interface ConfigLoader {

    LoadBalancerConfig load(String path);
}
