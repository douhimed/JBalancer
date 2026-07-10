package org.adex.backend;

import java.util.Set;

public interface BackendRegistry {

    void register(Backend backend);

    void unregister(Backend backend);

    Set<Backend> backends();

    Set<Backend> available();
}
