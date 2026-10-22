package com.example.inventory.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductServiceTest {
    private final ProductService service = new ProductService(null);

    @Test
    void rejectsMissingPerishableFields() {
        assertThrows(ProductService.ValidationException.class, () ->
                service.create(1, "Milk", BigDecimal.ONE, 1, 0,
                        "PERISHABLE", null, null, null));
    }

    @Test
    void rejectsSubtypeFieldsBelongingToTheOtherSubtype() {
        assertThrows(ProductService.ValidationException.class, () ->
                service.create(1, "Milk", BigDecimal.ONE, 1, 0,
                        "PERISHABLE", Date.valueOf("2027-01-01"), BigDecimal.ONE, 12));
        assertThrows(ProductService.ValidationException.class, () ->
                service.create(1, "Screwdriver", BigDecimal.ONE, 1, 0,
                        "NON_PERISHABLE", Date.valueOf("2027-01-01"), null, 12));
    }
}
