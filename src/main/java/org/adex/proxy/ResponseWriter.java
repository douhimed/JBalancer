package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;

import java.net.http.HttpResponse;

public interface ResponseWriter {

    void write(HttpExchange exchange, HttpResponse<byte[]> response) throws Exception;

    void error(HttpExchange exchange, int status, String message) throws Exception;
}
