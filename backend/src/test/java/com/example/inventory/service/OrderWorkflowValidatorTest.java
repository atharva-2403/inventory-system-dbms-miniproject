package com.example.inventory.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderWorkflowValidatorTest {
    @Test
    void preventsReceivingAnAlreadyReceivedOrder() {
        assertThrows(IllegalStateException.class,
                () -> OrderWorkflowValidator.validateReceiveStatus("RECEIVED"));
        assertDoesNotThrow(() -> OrderWorkflowValidator.validateReceiveStatus("PENDING"));
    }

    @Test
    void preventsCompletingAnAlreadyCompletedOrder() {
        assertThrows(IllegalStateException.class,
                () -> OrderWorkflowValidator.validateCompleteStatus("COMPLETED"));
        assertDoesNotThrow(() -> OrderWorkflowValidator.validateCompleteStatus("PENDING"));
    }
}
