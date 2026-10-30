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

    @Test
    void rejectsUnknownEmployeeRole() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.setRole(1, new com.example.inventory.model.EmployeeRole("BOSS", null, null, null, null)));
    }

    @Test
    void rejectsManagerWithoutDepartment() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.setRole(1, new com.example.inventory.model.EmployeeRole("MANAGER", " ", 3, null, null)));
    }

    @Test
    void rejectsWarehouseStaffWithoutWarehouse() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.setRole(1, new com.example.inventory.model.EmployeeRole("WAREHOUSE_STAFF", null, null, null, "09:00-17:00")));
    }

    @Test
    void rejectsInvalidSupplierProductLink() {
        assertThrows(MasterDataService.ValidationException.class, () ->
                service.linkProduct(1, 0));
    }
}
