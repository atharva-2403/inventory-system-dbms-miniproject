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
    private void check(int id){if(id<=0)throw new ValidationException("ID must be positive.");}
    private void supplierName(Supplier x){if(x==null||x.name()==null||x.name().isBlank())throw new ValidationException("Supplier name is required.");}
    private void customerNames(Customer x){if(x==null)throw new ValidationException("Customer is required.");names(x.firstName(),x.lastName());}
    private void employeeFields(Employee x){if(x==null)throw new ValidationException("Employee is required.");names(x.firstName(),x.lastName());if(x.dob()==null)throw new ValidationException("Date of birth is required.");if(x.dob().isAfter(java.time.LocalDate.now()))throw new ValidationException("Date of birth cannot be in the future.");}
    private void warehouseFields(Warehouse x){if(x==null||x.capacity()<0)throw new ValidationException("Capacity must be zero or greater.");}
    private void names(String a,String b){if(a==null||a.isBlank()||b==null||b.isBlank())throw new ValidationException("First and last names are required.");}
}
