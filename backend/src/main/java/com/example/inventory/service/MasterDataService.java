package com.example.inventory.service;

import com.example.inventory.dao.MasterDataDao;
import com.example.inventory.model.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class MasterDataService {
    public static class ValidationException extends RuntimeException { public ValidationException(String m){super(m);} }
    private final MasterDataDao dao;
    public MasterDataService(MasterDataDao dao){this.dao=dao;}
    public List<Supplier> suppliers()throws SQLException{return dao.suppliers();} public Optional<Supplier> supplier(int id)throws SQLException{check(id);return dao.supplier(id);}
    public Supplier create(Supplier x)throws SQLException{supplierName(x);return dao.createSupplier(x);}
    public Optional<Supplier> update(int id,Supplier x)throws SQLException{check(id);supplierName(x);return dao.updateSupplier(id,x);} public boolean deleteSupplier(int id)throws SQLException{check(id);return dao.deleteSupplier(id);}
    public List<Customer> customers()throws SQLException{return dao.customers();} public Optional<Customer> customer(int id)throws SQLException{check(id);return dao.customer(id);}
    public Customer create(Customer x)throws SQLException{customerNames(x);return dao.createCustomer(x);} public Optional<Customer> update(int id,Customer x)throws SQLException{check(id);customerNames(x);return dao.updateCustomer(id,x);} public boolean deleteCustomer(int id)throws SQLException{check(id);return dao.deleteCustomer(id);}
    public List<Employee> employees()throws SQLException{return dao.employees();} public Optional<Employee> employee(int id)throws SQLException{check(id);return dao.employee(id);}
    public Employee create(Employee x)throws SQLException{employeeFields(x);return dao.createEmployee(x);} public Optional<Employee> update(int id,Employee x)throws SQLException{check(id);employeeFields(x);return dao.updateEmployee(id,x);} public boolean deleteEmployee(int id)throws SQLException{check(id);return dao.deleteEmployee(id);}
    public List<Warehouse> warehouses()throws SQLException{return dao.warehouses();} public Optional<Warehouse> warehouse(int id)throws SQLException{check(id);return dao.warehouse(id);}
    public Warehouse create(Warehouse x)throws SQLException{warehouseFields(x);return dao.createWarehouse(x);} public Optional<Warehouse> update(int id,Warehouse x)throws SQLException{check(id);warehouseFields(x);return dao.updateWarehouse(id,x);} public boolean deleteWarehouse(int id)throws SQLException{check(id);return dao.deleteWarehouse(id);}
    public List<Contact> contacts(String kind, int ownerId) throws SQLException {
        check(ownerId);
        return dao.contacts(kind, ownerId);
    }
    public Contact addContact(String kind, int ownerId, String value) throws SQLException {
        check(ownerId);
        if (value == null || value.isBlank()) throw new ValidationException("Contact value is required.");
        String v = value.trim();
        if (v.length() > 150) throw new ValidationException("Contact must be 150 characters or fewer.");
        return dao.addContact(kind, ownerId, v);
    }
    public boolean deleteContact(String kind, int ownerId, int contactId) throws SQLException {
        check(ownerId);
        check(contactId);
        return dao.deleteContact(kind, ownerId, contactId);
    }
    public List<Employee> managers() throws SQLException {
        return dao.managers();
    }
    public EmployeeRole role(int employeeId) throws SQLException {
        check(employeeId);
        return dao.role(employeeId);
    }
    public EmployeeRole setRole(int employeeId, EmployeeRole r) throws SQLException {
        check(employeeId);
        if (r == null || r.type() == null) throw new ValidationException("Role type is required.");
        switch (r.type()) {
            case "NONE" -> { }
            case "MANAGER" -> {
                if (r.department() == null || r.department().isBlank()) throw new ValidationException("Department is required for a manager.");
                if (r.department().trim().length() > 100) throw new ValidationException("Department must be 100 characters or fewer.");
                if (r.teamSize() != null && r.teamSize() < 0) throw new ValidationException("Team size must be zero or greater.");
            }
            case "WAREHOUSE_STAFF" -> {
                if (r.warehouseId() == null || r.warehouseId() <= 0) throw new ValidationException("Warehouse is required for warehouse staff.");
                if (r.shiftTiming() == null || r.shiftTiming().isBlank()) throw new ValidationException("Shift timing is required for warehouse staff.");
                if (r.shiftTiming().trim().length() > 100) throw new ValidationException("Shift timing must be 100 characters or fewer.");
            }
            default -> throw new ValidationException("Role must be NONE, MANAGER or WAREHOUSE_STAFF.");
        }
        dao.setRole(employeeId, r);
        return dao.role(employeeId);
    }
    public List<LinkedProduct> supplierProducts(int supplierId) throws SQLException {
        check(supplierId);
        return dao.supplierProducts(supplierId);
    }
    public LinkedProduct linkProduct(int supplierId, int productId) throws SQLException {
        check(supplierId);
        check(productId);
        return dao.linkProduct(supplierId, productId);
    }
    public boolean unlinkProduct(int supplierId, int productId) throws SQLException {
        check(supplierId);
        check(productId);
        return dao.unlinkProduct(supplierId, productId);
    }
    private void check(int id){if(id<=0)throw new ValidationException("ID must be positive.");}
    private void supplierName(Supplier x){if(x==null||x.name()==null||x.name().isBlank())throw new ValidationException("Supplier name is required.");}
    private void customerNames(Customer x){if(x==null)throw new ValidationException("Customer is required.");names(x.firstName(),x.lastName());}
    private void employeeFields(Employee x){if(x==null)throw new ValidationException("Employee is required.");names(x.firstName(),x.lastName());if(x.dob()==null)throw new ValidationException("Date of birth is required.");if(x.dob().isAfter(java.time.LocalDate.now()))throw new ValidationException("Date of birth cannot be in the future.");}
    private void warehouseFields(Warehouse x){if(x==null||x.capacity()<0)throw new ValidationException("Capacity must be zero or greater.");}
    private void names(String a,String b){if(a==null||a.isBlank()||b==null||b.isBlank())throw new ValidationException("First and last names are required.");}
}
