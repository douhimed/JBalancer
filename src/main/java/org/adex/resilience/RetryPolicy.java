package org.adex.resilience;

public interface RetryPolicy {

    boolean shouldRetry(Exception ex);

    int maxRetries();
}
