package com.example.inventory.service;

import com.example.inventory.dao.ProductDao;
import com.example.inventory.model.Product;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ProductService {
    public static final class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> findAll() throws SQLException {
        return productDao.findAll();
    }

    public Optional<Product> findById(int id) throws SQLException {
        validateId(id);
        return productDao.findById(id);
    }

    public Product create(int categoryId, String name, BigDecimal price, int quantity, int reorderLevel,
                          String subtype, Date expiryDate, BigDecimal temperature, Integer warrantyMonths)
            throws SQLException {
        validate(categoryId, name, price, quantity, reorderLevel, subtype, expiryDate, temperature, warrantyMonths);
        return productDao.create(categoryId, name.trim(), price, quantity, reorderLevel, subtype,
                expiryDate, temperature, warrantyMonths);
    }


    public Optional<Product> update(int id, int categoryId, String name, BigDecimal price, int quantity,
                                    int reorderLevel, String subtype, Date expiryDate,
                                    BigDecimal temperature, Integer warrantyMonths) throws SQLException {
        validateId(id);
        validate(categoryId, name, price, quantity, reorderLevel, subtype, expiryDate, temperature, warrantyMonths);
        return productDao.update(id, categoryId, name.trim(), price, quantity, reorderLevel, subtype,
                expiryDate, temperature, warrantyMonths);
    }

    public boolean delete(int id) throws SQLException {
        validateId(id);
        return productDao.delete(id);
    }

    private void validate(int categoryId, String name, BigDecimal price, int quantity, int reorderLevel,
                          String subtype, Date expiryDate, BigDecimal temperature, Integer warrantyMonths) {
        if (categoryId <= 0) throw new ValidationException("Category ID must be positive.");
        if (name == null || name.isBlank()) throw new ValidationException("Product name is required.");
        if (name.trim().length() > 150) throw new ValidationException("Product name must be 150 characters or fewer.");
        if (price == null || price.signum() < 0) throw new ValidationException("Unit price must be zero or greater.");
        if (quantity < 0) throw new ValidationException("Quantity in stock must be zero or greater.");
        if (reorderLevel < 0) throw new ValidationException("Reorder level must be zero or greater.");
        if (!"PERISHABLE".equals(subtype) && !"NON_PERISHABLE".equals(subtype)) {
            throw new ValidationException("Product subtype must be PERISHABLE or NON_PERISHABLE.");
        }
        if ("PERISHABLE".equals(subtype) && (expiryDate == null || temperature == null)) {
            throw new ValidationException("Perishable products require expiry date and storage temperature.");
        }
        if ("PERISHABLE".equals(subtype) && warrantyMonths != null) {
            throw new ValidationException("Perishable products cannot have a warranty period.");
        }
        if ("NON_PERISHABLE".equals(subtype) && (warrantyMonths == null || warrantyMonths < 0)) {
            throw new ValidationException("Non-perishable products require a valid warranty period.");
        }
        if ("NON_PERISHABLE".equals(subtype) && (expiryDate != null || temperature != null)) {
            throw new ValidationException("Non-perishable products cannot have perishable-product fields.");
        }
    }

    private void validateId(int id) {
        if (id <= 0) throw new ValidationException("Product ID must be positive.");
    }
}
