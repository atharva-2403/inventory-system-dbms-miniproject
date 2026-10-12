package com.example.inventory.controller;

import com.example.inventory.model.Category;
import com.example.inventory.server.HttpUtil;
import com.example.inventory.service.CategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class CategoryController {
    private final CategoryService service;
    private final ObjectMapper mapper;

    public CategoryController(CategoryService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod().toUpperCase();
            Integer id = parseId(exchange.getRequestURI().getPath());
            switch (method) {
                case "GET" -> get(exchange, id);
                case "POST" -> create(exchange, id);
                case "PUT" -> update(exchange, id);
                case "DELETE" -> delete(exchange, id);
                case "OPTIONS" -> HttpUtil.sendEmpty(exchange, 204);
                default -> HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Method is not supported.");
            }
        } catch (CategoryService.ValidationException exception) {
            HttpUtil.sendError(exchange, 400, "VALIDATION_ERROR", exception.getMessage());
        } catch (SQLException exception) {
            if (isForeignKeyViolation(exception)) {
                HttpUtil.sendError(exchange, 409, "REFERENCED_RECORD", "Category is still referenced by a product.");
            } else if (isDuplicate(exception)) {
                HttpUtil.sendError(exchange, 409, "DUPLICATE_CATEGORY", "A category with that name already exists.");
            } else {
                System.err.println("Category database operation failed: " + exception.getMessage());
                HttpUtil.sendError(exchange, 500, "DATABASE_ERROR", "The category operation could not be completed.");
            }
        }
    }

    private void get(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendJson(exchange, 200, service.findAll());
            return;
        }
        var category = service.findById(id);
        if (category.isEmpty()) {
            HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Category was not found.");
            return;
        }
        HttpUtil.sendJson(exchange, 200, category.get());
    }

    private void create(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id != null) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "POST is only supported on /api/categories.");
            return;
        }
        JsonNode body = mapper.readTree(HttpUtil.readBody(exchange));
        HttpUtil.sendJson(exchange, 201, service.create(text(body, "name"), optionalText(body, "description")));
    }

    private void update(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendError(exchange, 400, "INVALID_PATH", "Category ID is required.");
            return;
        }
        JsonNode body = mapper.readTree(HttpUtil.readBody(exchange));
        var category = service.update(id, text(body, "name"), optionalText(body, "description"));
        if (category.isEmpty()) {
            HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Category was not found.");
            return;
        }
        HttpUtil.sendJson(exchange, 200, category.get());
    }

    private void delete(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendError(exchange, 400, "INVALID_PATH", "Category ID is required.");
            return;
        }
        if (!service.delete(id)) {
            HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Category was not found.");
            return;
        }
        HttpUtil.sendEmpty(exchange, 204);
    }

    private String text(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String optionalText(JsonNode body, String field) {
        return text(body, field);
    }

    private Integer parseId(String path) {
        String prefix = "/api/categories";
        if (path.equals(prefix) || path.equals(prefix + "/")) {
            return null;
        }
        String value = path.substring(prefix.length()).replace("/", "");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new CategoryService.ValidationException("Category ID must be a number.");
        }
    }

    private boolean isDuplicate(SQLException exception) {
        return exception.getErrorCode() == 1062;
    }

    private boolean isForeignKeyViolation(SQLException exception) {
        return exception.getErrorCode() == 1451;
    }
}
