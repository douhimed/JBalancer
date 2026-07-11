package org.adex.config;

import java.net.URISyntaxException;

public interface ConfigLoader {

    LoadBalancerConfig load(String path) throws URISyntaxException;
}
