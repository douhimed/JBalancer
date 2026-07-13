package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DefaultResponseWriter implements ResponseWriter {

    @Override
    public void write(HttpExchange exchange, HttpResponse<byte[]> response) throws Exception {
        exchange.getResponseHeaders().putAll(response.headers().map());
        byte[] body = response.body();
        exchange.sendResponseHeaders(response.statusCode(), body.length);
        exchange.getResponseBody().write(body);
    }

    @Override
    public void error(HttpExchange exchange, int status, String message) throws Exception {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }
}