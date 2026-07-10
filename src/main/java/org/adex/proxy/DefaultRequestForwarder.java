package org.adex.proxy;

import com.sun.net.httpserver.HttpExchange;
import org.adex.backend.Backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DefaultRequestForwarder implements RequestForwarder {

    private final HttpClient  httpClient;

    public DefaultRequestForwarder() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public HttpResponse<byte[]> forward(Backend backend, HttpExchange exchange) throws Exception {

        final URI uri = URI.create(backend.url() + exchange.getRequestURI());

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .method(exchange.getRequestMethod(), HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
