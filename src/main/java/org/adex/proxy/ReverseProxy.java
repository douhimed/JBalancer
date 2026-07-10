package org.adex.proxy;

public interface ReverseProxy {

    void forward(Object req, Object resp);
}
