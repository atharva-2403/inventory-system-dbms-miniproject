package com.example.inventory.service;

public final class OrderWorkflowValidator {
    private OrderWorkflowValidator() {
    }

    public static void validateReceiveStatus(String status) {
        if ("RECEIVED".equals(status)) {
            throw new IllegalStateException("Purchase order has already been received.");
        }
        if (!"PENDING".equals(status) && !"DRAFT".equals(status)) {
            throw new IllegalStateException("Purchase order cannot be received in its current status.");
        }
    }

    public static void validateCompleteStatus(String status) {
        if ("COMPLETED".equals(status)) {
            throw new IllegalStateException("Sales order has already been completed.");
        }
        if (!"PENDING".equals(status) && !"DRAFT".equals(status)) {
            throw new IllegalStateException("Sales order cannot be completed in its current status.");
        }
    }
}
