package com.example.inventory.model;

import java.time.LocalDateTime;

public record StockTransaction(int id, int productId, String transactionType, int quantity,
                               LocalDateTime transactionDate, Integer recordedByEmployeeId,
                               Integer purchaseOrderId, Integer salesOrderId) {}
