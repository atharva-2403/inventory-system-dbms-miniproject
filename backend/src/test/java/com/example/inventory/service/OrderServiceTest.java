package com.example.inventory.service;

import com.example.inventory.model.PurchaseOrder;
import com.example.inventory.model.SalesOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    private final OrderService service = new OrderService(null);
    private static final Date DATE = Date.valueOf("2026-01-01");

    @Test
    void rejectsDuplicateProductsInPurchaseAndSale() {
        var purchase = new PurchaseOrder.Detail(0, 7, 1, BigDecimal.ONE);
        assertThrows(OrderService.ValidationException.class, () ->
                service.createPurchase(1, DATE, null, List.of(purchase,
                        new PurchaseOrder.Detail(0, 7, 2, BigDecimal.TEN))));

        var sale = new SalesOrder.Detail(0, 9, 1, BigDecimal.ONE);
        assertThrows(OrderService.ValidationException.class, () ->
                service.createSale(1, DATE, List.of(sale,
                        new SalesOrder.Detail(0, 9, 2, BigDecimal.TEN))));
    }

    @Test
    void rejectsInvalidEmployeeOnOrderActions() {
        assertThrows(OrderService.ValidationException.class, () -> service.receive(1, 0));
        assertThrows(OrderService.ValidationException.class, () -> service.complete(1, -1));
    }
}
