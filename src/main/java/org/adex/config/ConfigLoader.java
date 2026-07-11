package org.adex.config;

import java.net.URISyntaxException;

public interface ConfigLoader {

    LoadBalancerConfig load(String path) throws URISyntaxException;

    static LoadBalancerConfig from(String filePath) throws URISyntaxException {
        if (filePath != null && (filePath.trim().endsWith(".yml") || filePath.trim().endsWith(".yaml"))) {
            return new YamlConfigLoader().load(filePath);
        }

        throw new IllegalArgumentException("Missing configuration file for loader or not defined yet : " + filePath);
    }

}
