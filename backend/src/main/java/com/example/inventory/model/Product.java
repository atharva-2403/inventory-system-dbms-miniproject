package com.example.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Product(
        int id,
        int categoryId,
        String categoryName,
        String name,
        BigDecimal unitPrice,
        int quantityInStock,
        int reorderLevel,
        BigDecimal stockValue,
        String subtype,
        LocalDate expiryDate,
        BigDecimal storageTemperature,
        Integer warrantyPeriodMonths) {
}
