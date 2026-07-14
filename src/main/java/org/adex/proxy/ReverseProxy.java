package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;

public interface ReverseProxy {

    void forward(HttpExchange exchange);

}
