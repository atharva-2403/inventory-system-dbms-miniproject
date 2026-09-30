package com.example.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesOrder(int id, int customerId, LocalDate orderDate, String status,
                         BigDecimal total, List<Detail> details) {
    public record Detail(int id, int productId, int quantity, BigDecimal unitPrice) {}
}
