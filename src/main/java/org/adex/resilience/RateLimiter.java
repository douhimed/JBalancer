package org.adex.resilience;

public interface RateLimiter {

    boolean allow(String clientId);

}
