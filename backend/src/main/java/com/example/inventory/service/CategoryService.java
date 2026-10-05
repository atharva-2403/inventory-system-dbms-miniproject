package com.example.inventory.service;

import com.example.inventory.dao.CategoryDao;
import com.example.inventory.model.Category;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class CategoryService {
    public static final class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public List<Category> findAll() throws SQLException {
        return categoryDao.findAll();
    }

    public Optional<Category> findById(int id) throws SQLException {
        validateId(id);
        return categoryDao.findById(id);
    }

    public Category create(String name, String description) throws SQLException {
        validate(name, description);
        return categoryDao.create(name.trim(), normalizeDescription(description));
    }

    public Optional<Category> update(int id, String name, String description) throws SQLException {
        validateId(id);
        validate(name, description);
        return categoryDao.update(id, name.trim(), normalizeDescription(description));
    }

    public boolean delete(int id) throws SQLException {
        validateId(id);
        return categoryDao.delete(id);
    }

    private void validate(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Category name is required.");
        }
        if (name.trim().length() > 100) {
            throw new ValidationException("Category name must be 100 characters or fewer.");
        }
        if (description != null && description.length() > 500) {
            throw new ValidationException("Description must be 500 characters or fewer.");
        }
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new ValidationException("Category ID must be positive.");
        }
    }
}
