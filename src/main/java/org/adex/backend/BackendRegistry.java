package org.adex.backend;

import java.util.Set;

public interface BackendRegistry {

    BackendRegistry register(Backend backend);
    void register(Set<Backend> backend);

    void unregister(String backendId);

    Set<Backend> allBackends();

    Set<Backend> onlyAvailableBackends();

    BackendRegistry upateStatus(String backendId, BackendStatus newStatus);

    BackendStatus getStatus(String backendId);
}
