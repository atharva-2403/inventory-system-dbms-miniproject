package com.example.inventory.dao;

import com.example.inventory.config.Database;
import com.example.inventory.model.Category;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CategoryDao {
    private final Database database;

    public CategoryDao(Database database) {
        this.database = database;
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, category_name, description " +
                "FROM categories ORDER BY category_name";
        List<Category> categories = new ArrayList<>();
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql);
             var results = statement.executeQuery()) {
            while (results.next()) {
                categories.add(map(results));
            }
        }
        return categories;
    }

    public Optional<Category> findById(int id) throws SQLException {
        String sql = "SELECT category_id, category_name, description " +
                "FROM categories WHERE category_id = ?";
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (var results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Category create(String name, String description) throws SQLException {
        String sql = "INSERT INTO categories (category_name, description) VALUES (?, ?)";
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Category ID was not generated");
                }
                return new Category(keys.getInt(1), name, description);
            }
        }
    }

    public Optional<Category> update(int id, String name, String description) throws SQLException {
        String sql = "UPDATE categories SET category_name = ?, description = ? WHERE category_id = ?";
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, id);
            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
        }
        return Optional.of(new Category(id, name, description));
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (var connection = database.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Category map(java.sql.ResultSet results) throws SQLException {
        return new Category(
                results.getInt("category_id"),
                results.getString("category_name"),
                results.getString("description"));
    }
}
