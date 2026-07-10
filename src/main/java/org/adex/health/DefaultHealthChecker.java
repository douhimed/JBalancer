package org.adex.health;

import org.adex.backend.Backend;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DefaultHealthChecker implements HealthChecker {

    private static final String HEALTH_ENDPOINT = "/health";

    private final HttpClient httpClient;


    public DefaultHealthChecker() {
        httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public boolean check(Backend backend) {

        try {
            URI uri = URI.create(backend.url() + HEALTH_ENDPOINT);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<Void> res = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }
}
