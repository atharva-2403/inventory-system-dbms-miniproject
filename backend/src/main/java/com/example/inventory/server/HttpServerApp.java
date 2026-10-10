package com.example.inventory.server;

import com.example.inventory.config.Database;
import com.example.inventory.config.DatabaseConfig;
import com.example.inventory.controller.CategoryController;
import com.example.inventory.controller.ProductController;
import com.example.inventory.controller.MasterDataController;
import com.example.inventory.controller.OrderController;
import com.example.inventory.dao.CategoryDao;
import com.example.inventory.dao.ProductDao;
import com.example.inventory.service.CategoryService;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.MasterDataService;
import com.example.inventory.service.OrderService;
import com.example.inventory.dao.MasterDataDao;
import com.example.inventory.dao.OrderDao;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;

public final class HttpServerApp {
    private final DatabaseConfig config;
    private final Database database;

    public HttpServerApp(DatabaseConfig config) {
        this.config = config;
        this.database = new Database(config);
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(config.serverPort()), 0);
        CategoryController categoryController = new CategoryController(
                new CategoryService(new CategoryDao(database)),
                com.example.inventory.util.JsonUtil.mapper());
        ProductController productController = new ProductController(
                new ProductService(new ProductDao(database)),
                com.example.inventory.util.JsonUtil.mapper());
        MasterDataService masterDataService = new MasterDataService(new MasterDataDao(database));
        OrderService orderService = new OrderService(new OrderDao(database));
        var mapper = com.example.inventory.util.JsonUtil.mapper();
        MasterDataController suppliers = new MasterDataController(masterDataService, mapper, "/api/suppliers", "supplier");
        MasterDataController warehouses = new MasterDataController(masterDataService, mapper, "/api/warehouses", "warehouse");
        MasterDataController customers = new MasterDataController(masterDataService, mapper, "/api/customers", "customer");
        MasterDataController employees = new MasterDataController(masterDataService, mapper, "/api/employees", "employee");
        OrderController purchaseOrders = new OrderController(orderService, mapper, "/api/purchase-orders", "purchase");
        OrderController salesOrders = new OrderController(orderService, mapper, "/api/sales-orders", "sale");
        OrderController stockTransactions = new OrderController(orderService, mapper, "/api/stock-transactions", "stock");
        server.createContext("/api/health", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtil.sendJson(exchange, 405, Map.of(
                        "error", Map.of(
                                "code", "METHOD_NOT_ALLOWED",
                                "message", "Only GET is supported for this endpoint."
                        )));
                return;
            }

            try (var connection = database.getConnection()) {
                HttpUtil.sendJson(exchange, 200, Map.of(
                        "status", "ok",
                        "database", connection.isValid(2) ? "connected" : "unavailable"
                ));
            } catch (SQLException exception) {
                System.err.println("Database health check failed: " + exception.getMessage());
                HttpUtil.sendJson(exchange, 503, Map.of(
                        "error", Map.of(
                                "code", "DATABASE_UNAVAILABLE",
                                "message", "The database is not available."
                        )));
            }
        });
        server.createContext("/api/categories", categoryController::handle);
        server.createContext("/api/products", productController::handle);
        server.createContext("/api/suppliers", suppliers::handle);
        server.createContext("/api/warehouses", warehouses::handle);
        server.createContext("/api/customers", customers::handle);
        server.createContext("/api/employees", employees::handle);
        server.createContext("/api/purchase-orders", purchaseOrders::handle);
        server.createContext("/api/sales-orders", salesOrders::handle);
        server.createContext("/api/stock-transactions", stockTransactions::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.printf("Inventory API listening on http://localhost:%d%n", config.serverPort());
    }
}
