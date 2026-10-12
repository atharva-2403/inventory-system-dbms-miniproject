package com.example.inventory.controller;

import com.example.inventory.server.HttpUtil;
import com.example.inventory.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;

public final class ProductController {
    private final ProductService service;
    private final ObjectMapper mapper;

    public ProductController(ProductService service, ObjectMapper mapper) {
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
        } catch (ProductService.ValidationException exception) {
            HttpUtil.sendError(exchange, 400, "VALIDATION_ERROR", exception.getMessage());
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1451) {
                HttpUtil.sendError(exchange, 409, "REFERENCED_RECORD", "Product is still referenced by another record.");
            } else if (exception.getErrorCode() == 1452) {
                HttpUtil.sendError(exchange, 400, "INVALID_REFERENCE", "The selected category does not exist.");
            } else {
                System.err.println("Product database operation failed: " + exception.getMessage());
                HttpUtil.sendError(exchange, 500, "DATABASE_ERROR", "The product operation could not be completed.");
            }
        } catch (RuntimeException exception) {
            System.err.println("Product request failed: " + exception.getMessage());
            HttpUtil.sendError(exchange, 500, "REQUEST_ERROR", "The product request could not be completed.");
        }
    }

    private void get(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendJson(exchange, 200, service.findAll());
            return;
        }
        var product = service.findById(id);
        if (product.isEmpty()) {
            HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Product was not found.");
        } else {
            HttpUtil.sendJson(exchange, 200, product.get());
        }
    }

    private void create(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id != null) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "POST is only supported on /api/products.");
            return;
        }
        Request product = read(exchange);
        HttpUtil.sendJson(exchange, 201, service.create(product.categoryId(), product.name(), product.price(),
                product.quantity(), product.reorderLevel(), product.subtype(), product.expiryDate(),
                product.temperature(), product.warrantyMonths()));
    }

    private void update(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendError(exchange, 400, "INVALID_PATH", "Product ID is required.");
            return;
        }
        var product = read(exchange);
        var updated = service.update(id, product.categoryId(), product.name(), product.price(), product.quantity(),
                product.reorderLevel(), product.subtype(), product.expiryDate(), product.temperature(),
                product.warrantyMonths());
        if (updated.isEmpty()) HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Product was not found.");
        else HttpUtil.sendJson(exchange, 200, updated.get());
    }

    private void delete(HttpExchange exchange, Integer id) throws IOException, SQLException {
        if (id == null) {
            HttpUtil.sendError(exchange, 400, "INVALID_PATH", "Product ID is required.");
            return;
        }
        if (!service.delete(id)) HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Product was not found.");
        else HttpUtil.sendEmpty(exchange, 204);
    }

    private Request read(HttpExchange exchange) throws IOException {
        JsonNode body = mapper.readTree(HttpUtil.readBody(exchange));
        return new Request(
                integer(body, "categoryId"),
                text(body, "name"),
                decimal(body, "unitPrice"),
                integer(body, "quantityInStock"),
                integer(body, "reorderLevel"),
                text(body, "subtype"),
                date(body, "expiryDate"),
                decimal(body, "storageTemperature"),
                nullableInteger(body, "warrantyPeriodMonths"));
    }

    private record Request(int categoryId, String name, BigDecimal price, int quantity, int reorderLevel,
                           String subtype, Date expiryDate, BigDecimal temperature, Integer warrantyMonths) {
    }

    private String text(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private int integer(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        if (value == null || !value.canConvertToInt()) throw new ProductService.ValidationException(field + " must be an integer.");
        return value.asInt();
    }

    private Integer nullableInteger(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        return value == null || value.isNull() ? null : integer(body, field);
    }

    private BigDecimal decimal(JsonNode body, String field) {
        String value = text(body, field);
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value); }
        catch (NumberFormatException exception) { throw new ProductService.ValidationException(field + " must be a number."); }
    }

    private Date date(JsonNode body, String field) {
        String value = text(body, field);
        if (value == null || value.isBlank()) return null;
        try { return Date.valueOf(value); }
        catch (IllegalArgumentException exception) { throw new ProductService.ValidationException(field + " must use YYYY-MM-DD format."); }
    }

    private Integer parseId(String path) {
        String prefix = "/api/products";
        if (path.equals(prefix) || path.equals(prefix + "/")) return null;
        try { return Integer.parseInt(path.substring(prefix.length()).replace("/", "")); }
        catch (NumberFormatException exception) { throw new ProductService.ValidationException("Product ID must be a number."); }
    }
}
