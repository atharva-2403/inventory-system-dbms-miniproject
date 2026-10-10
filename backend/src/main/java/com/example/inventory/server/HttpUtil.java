package com.example.inventory.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class HttpUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private HttpUtil() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] response = OBJECT_MAPPER.writeValueAsBytes(body);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(statusCode, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String code, String message)
            throws IOException {
        sendJson(exchange, statusCode, Map.of("error", Map.of("code", code, "message", message)));
    }

    public static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:5173");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }
}
