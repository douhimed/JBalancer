package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;
import org.adex.backend.Backend;

import java.net.URI;
import java.net.http.*;

public class DefaultRequestForwarder implements RequestForwarder {


    private final HttpClient client;

    public DefaultRequestForwarder() {
        this.client = HttpClient.newBuilder().build();
    }

    @Override
    public HttpResponse<byte[]> forward(Backend backend, HttpExchange exchange) throws Exception {
        URI uri = URI.create(backend.url() + exchange.getRequestURI());
        HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .method(exchange.getRequestMethod(),
                                HttpRequest.BodyPublishers.ofByteArray(exchange.getRequestBody().readAllBytes()))
                        .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}