package org.adex.mockbackendservice;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SimpleBackendService {

    static void main(String[] args) throws Exception {

        for (Integer port : List.of(9001, 9002, 9003)) {

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            if (port != 9003) {
                server.createContext("/", exchange -> {
                    String response = "Response from backend " + port;
                    byte[] body = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                });
            }

            server.createContext("/health", exchange -> {
                if (port == 9003) {
                    byte[] body = "DOWN".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(503, body.length);
                    exchange.getResponseBody().write(body);
                } else {
                    byte[] body = "UP".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                }
                exchange.close();
            });

            server.start();
            System.out.printf("Backend running on port %d (%s)%n", port, port == 9003 ? "UNHEALTHY" : "HEALTHY");
        }
    }
}