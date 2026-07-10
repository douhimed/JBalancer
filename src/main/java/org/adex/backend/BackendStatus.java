package org.adex.backend;

public enum BackendStatus {
    UP, DOWN, UNKNOW;

    public boolean isUP() {
        return this == UP;
    }
}
