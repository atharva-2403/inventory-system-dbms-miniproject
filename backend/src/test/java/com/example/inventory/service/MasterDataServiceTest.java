package com.example.inventory.service;

import com.example.inventory.model.Employee;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MasterDataServiceTest {
    private final MasterDataService service = new MasterDataService(null);

    @Test
    void rejectsFutureEmployeeBirthDate() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.create(new Employee(0, "A", "B", LocalDate.now().plusDays(1))));
    }

    @Test
    void rejectsBlankContact() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.addContact("supplier", 1, "   "));
    }

    @Test
    void rejectsTooLongContact() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.addContact("supplier", 1, "x".repeat(151)));
    }

    @Test
    void rejectsInvalidContactOwnerId() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.addContact("customer", 0, "a@b.com"));
    }
}
