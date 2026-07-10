package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;
import org.adex.backend.Backend;

import java.net.http.HttpResponse;

public interface RequestForwarder {

    HttpResponse<byte[]> forward(Backend backend, HttpExchange exchange) throws Exception;

}
