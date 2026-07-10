package org.adex.backend;

import java.util.Set;

public class DefaultBackendRegistry implements BackendRegistry {

    @Override
    public void register(Backend backend) {

    }

    @Override
    public void unregister(Backend backend) {

    }

    @Override
    public Set<Backend> backends() {
        return Set.of();
    }

    @Override
    public Set<Backend> available() {
        return Set.of();
    }
}
