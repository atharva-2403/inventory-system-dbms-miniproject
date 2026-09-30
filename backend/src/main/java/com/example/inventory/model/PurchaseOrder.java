package com.example.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrder(int id, int supplierId, LocalDate orderDate, String status,
                            Integer approvedByManagerId, BigDecimal total, List<Detail> details) {
    public record Detail(int id, int productId, int quantity, BigDecimal unitPrice) {}
}
