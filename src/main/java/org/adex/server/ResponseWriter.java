package org.adex.server;

import com.sun.net.httpserver.HttpExchange;

import java.net.http.HttpResponse;

public interface ResponseWriter {

    void write(HttpExchange exchange, HttpResponse<byte[]> response) throws Exception;

    void write(HttpExchange exchange, int status, String message) throws Exception;
}
