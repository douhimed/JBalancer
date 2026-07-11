package org.adex.backend;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultBackendRegistry implements BackendRegistry {

    private final Map<String, Backend> backends = new ConcurrentHashMap<>();
    private final Map<String, BackendStatus> statuses = new ConcurrentHashMap<>();

    @Override
    public BackendRegistry register(Backend backend) {
        backends.put(backend.id(), backend);
        statuses.put(backend.id(), BackendStatus.UNKNOW);
        return this;
    }

    @Override
    public BackendRegistry register(Set<Backend> backendsSet) {
        backendsSet.forEach(this::register);
        return this;
    }

    @Override
    public void unregister(String backendId) {
        backends.remove(backendId);
        statuses.remove(backendId);
    }

    @Override
    public Set<Backend> allBackends() {
        return new HashSet<>(backends.values());
    }

    @Override
    public Set<Backend> onlyAvailableBackends() {
        return backends.entrySet()
                .stream()
                .filter(entry -> statuses.get(entry.getKey()).isUP())
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    @Override
    public BackendRegistry upateStatus(String backendId, BackendStatus newStatus) {
        if (!backends.containsKey(backendId)) {
            throw new IllegalArgumentException(backendId + " not found");
        }

        statuses.put(backendId, newStatus);
        return this;
    }

    @Override
    public BackendStatus getStatus(String backendId) {
        return statuses.get(backendId);
    }
}
