package org.adex.health;

import org.adex.backend.Backend;

public class DefaultHealthChecker implements HealthChecker {
    @Override
    public boolean check(Backend backend) {
        return false;
    }
}
