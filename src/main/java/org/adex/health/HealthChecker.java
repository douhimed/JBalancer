package org.adex.health;

import org.adex.backend.Backend;

public interface HealthChecker {

    boolean check(Backend backend);

}
