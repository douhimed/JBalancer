package org.adex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class YamlConfigLoader implements ConfigLoader {

    private final ObjectMapper mapper;

    public YamlConfigLoader() {

        mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    @Override
    public LoadBalancerConfig load(String filePath) throws URISyntaxException {
        Objects.requireNonNull(filePath, "Configuration file cannot be null");

        Path path = Path.of(Objects.requireNonNull(YamlConfigLoader.class.getClassLoader().getResource(filePath))
                .toURI());

        validate(path);

        try(InputStream inputStream = Files.newInputStream(path)){
            return mapper.readValue(inputStream, LoadBalancerConfig.class);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read configuration file: " + filePath, e);
        }
    }

    private void validate(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Configuration file does not exist: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a file: " + path);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Configuration file is not readable: " + path);
        }
    }
}
