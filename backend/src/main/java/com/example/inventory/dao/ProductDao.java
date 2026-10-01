package com.example.inventory.dao;

import com.example.inventory.config.Database;
import com.example.inventory.model.Product;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProductDao {
    private final Database database;

    public ProductDao(Database database) {
        this.database = database;
    }

    public List<Product> findAll() throws SQLException {
        String sql = """
                SELECT p.product_id, p.category_id, c.category_name, p.name, p.unit_price,
                       p.quantity_in_stock, p.reorder_level,
                       CASE WHEN pp.product_id IS NOT NULL THEN 'PERISHABLE'
                            WHEN np.product_id IS NOT NULL THEN 'NON_PERISHABLE'
                            ELSE NULL END AS subtype,
                       pp.expiry_date, pp.storage_temperature, np.warranty_period_months
                FROM products p
                JOIN categories c ON c.category_id = p.category_id
                LEFT JOIN perishable_products pp ON pp.product_id = p.product_id
                LEFT JOIN non_perishable_products np ON np.product_id = p.product_id
                ORDER BY p.name
                """;
        List<Product> products = new ArrayList<>();
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql);
             var results = statement.executeQuery()) {
            while (results.next()) {
                products.add(map(results));
            }
        }
        return products;
    }

    public Optional<Product> findById(int id) throws SQLException {
        return findAll().stream().filter(product -> product.id() == id).findFirst();
    }

    public Product create(
            int categoryId,
            String name,
            BigDecimal unitPrice,
            int quantity,
            int reorderLevel,
            String subtype,
            Date expiryDate,
            BigDecimal storageTemperature,
            Integer warrantyMonths) throws SQLException {
        String insert = """
                INSERT INTO products (category_id, name, unit_price, quantity_in_stock, reorder_level)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (var connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id;
                try (var statement = connection.prepareStatement(insert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, categoryId);
                    statement.setString(2, name);
                    statement.setBigDecimal(3, unitPrice);
                    statement.setInt(4, quantity);
                    statement.setInt(5, reorderLevel);
                    statement.executeUpdate();
                    try (var keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Product ID was not generated");
                        }
                        id = keys.getInt(1);
                    }
                }
                insertSubtype(connection, id, subtype, expiryDate, storageTemperature, warrantyMonths);
                connection.commit();
                return findById(id).orElseThrow();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Optional<Product> update(
            int id,
            int categoryId,
            String name,
            BigDecimal unitPrice,
            int quantity,
            int reorderLevel,
            String subtype,
            Date expiryDate,
            BigDecimal storageTemperature,
            Integer warrantyMonths) throws SQLException {
        String update = """
                UPDATE products
                SET category_id = ?, name = ?, unit_price = ?, quantity_in_stock = ?, reorder_level = ?
                WHERE product_id = ?
                """;
        try (var connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var statement = connection.prepareStatement(update)) {
                    statement.setInt(1, categoryId);
                    statement.setString(2, name);
                    statement.setBigDecimal(3, unitPrice);
                    statement.setInt(4, quantity);
                    statement.setInt(5, reorderLevel);
                    statement.setInt(6, id);
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return Optional.empty();
                    }
                }
                try (var statement = connection.prepareStatement(
                        "DELETE FROM perishable_products WHERE product_id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                try (var statement = connection.prepareStatement(
                        "DELETE FROM non_perishable_products WHERE product_id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                insertSubtype(connection, id, subtype, expiryDate, storageTemperature, warrantyMonths);
                connection.commit();
                return findById(id);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        try (var connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var statement = connection.prepareStatement(
                        "DELETE FROM perishable_products WHERE product_id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                try (var statement = connection.prepareStatement(
                        "DELETE FROM non_perishable_products WHERE product_id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                boolean deleted;
                try (var statement = connection.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                    statement.setInt(1, id);
                    deleted = statement.executeUpdate() > 0;
                }
                connection.commit();
                return deleted;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void insertSubtype(
            java.sql.Connection connection,
            int id,
            String subtype,
            Date expiryDate,
            BigDecimal storageTemperature,
            Integer warrantyMonths) throws SQLException {
        if ("PERISHABLE".equals(subtype)) {
            try (var statement = connection.prepareStatement("""
                    INSERT INTO perishable_products (product_id, expiry_date, storage_temperature)
                    VALUES (?, ?, ?)
                    """)) {
                statement.setInt(1, id);
                statement.setDate(2, expiryDate);
                statement.setBigDecimal(3, storageTemperature);
                statement.executeUpdate();
            }
        } else if ("NON_PERISHABLE".equals(subtype)) {
            try (var statement = connection.prepareStatement("""
                    INSERT INTO non_perishable_products (product_id, warranty_period_months)
                    VALUES (?, ?)
                    """)) {
                statement.setInt(1, id);
                statement.setInt(2, warrantyMonths);
                statement.executeUpdate();
            }
        }
    }

    private Product map(java.sql.ResultSet results) throws SQLException {
        Date expiry = results.getDate("expiry_date");
        BigDecimal price = results.getBigDecimal("unit_price");
        int quantity = results.getInt("quantity_in_stock");
        return new Product(
                results.getInt("product_id"),
                results.getInt("category_id"),
                results.getString("category_name"),
                results.getString("name"),
                price,
                quantity,
                results.getInt("reorder_level"),
                price.multiply(BigDecimal.valueOf(quantity)),
                results.getString("subtype"),
                expiry == null ? null : expiry.toLocalDate(),
                results.getBigDecimal("storage_temperature"),
                (Integer) results.getObject("warranty_period_months"));
    }
}
