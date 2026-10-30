package com.example.inventory.dao;

import com.example.inventory.config.Database;
import com.example.inventory.model.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MasterDataDao {
    private final Database database;
    public MasterDataDao(Database database) { this.database = database; }

    public List<Supplier> suppliers() throws SQLException {
        List<Supplier> out = new ArrayList<>();
        try (var c=database.getConnection(); var s=c.prepareStatement("SELECT supplier_id,name,street,city,state,pincode FROM suppliers ORDER BY name"); var r=s.executeQuery()) {
            while(r.next()) out.add(new Supplier(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)));
        } return out;
    }
    public Optional<Supplier> supplier(int id) throws SQLException { return suppliers().stream().filter(x->x.id()==id).findFirst(); }
    public Supplier createSupplier(Supplier x) throws SQLException { return insertSupplier(x); }
    private Supplier insertSupplier(Supplier x) throws SQLException {
        String q="INSERT INTO suppliers(name,street,city,state,pincode) VALUES(?,?,?,?,?)";
        try(var c=database.getConnection();var s=c.prepareStatement(q,java.sql.Statement.RETURN_GENERATED_KEYS)){bind(s,x.name(),x.street(),x.city(),x.state(),x.pincode());s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();return new Supplier(k.getInt(1),x.name(),x.street(),x.city(),x.state(),x.pincode());}}
    }
    public Optional<Supplier> updateSupplier(int id,Supplier x) throws SQLException {
        try(var c=database.getConnection();var s=c.prepareStatement("UPDATE suppliers SET name=?,street=?,city=?,state=?,pincode=? WHERE supplier_id=?")){bind(s,x.name(),x.street(),x.city(),x.state(),x.pincode());s.setInt(6,id);if(s.executeUpdate()==0)return Optional.empty();return Optional.of(new Supplier(id,x.name(),x.street(),x.city(),x.state(),x.pincode()));}
    }
    public boolean deleteSupplier(int id)throws SQLException{return delete("suppliers","supplier_id",id);}

    public List<Customer> customers() throws SQLException { List<Customer> o=new ArrayList<>(); try(var c=database.getConnection();var s=c.prepareStatement("SELECT customer_id,first_name,last_name,street,city,state,pincode FROM customers ORDER BY last_name,first_name");var r=s.executeQuery()){while(r.next())o.add(new Customer(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7)));}return o;}
    public Optional<Customer> customer(int id)throws SQLException{return customers().stream().filter(x->x.id()==id).findFirst();}
    public Customer createCustomer(Customer x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("INSERT INTO customers(first_name,last_name,street,city,state,pincode) VALUES(?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS)){bind(s,x.firstName(),x.lastName(),x.street(),x.city(),x.state(),x.pincode());s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();return new Customer(k.getInt(1),x.firstName(),x.lastName(),x.street(),x.city(),x.state(),x.pincode());}}}
    public Optional<Customer> updateCustomer(int id,Customer x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("UPDATE customers SET first_name=?,last_name=?,street=?,city=?,state=?,pincode=? WHERE customer_id=?")){bind(s,x.firstName(),x.lastName(),x.street(),x.city(),x.state(),x.pincode());s.setInt(7,id);if(s.executeUpdate()==0)return Optional.empty();return Optional.of(new Customer(id,x.firstName(),x.lastName(),x.street(),x.city(),x.state(),x.pincode()));}}
    public boolean deleteCustomer(int id)throws SQLException{return delete("customers","customer_id",id);}

    public List<Employee> employees() throws SQLException {List<Employee>o=new ArrayList<>();try(var c=database.getConnection();var s=c.prepareStatement("SELECT employee_id,first_name,last_name,dob FROM employees ORDER BY last_name,first_name");var r=s.executeQuery()){while(r.next())o.add(new Employee(r.getInt(1),r.getString(2),r.getString(3),r.getDate(4).toLocalDate()));}return o;}
    public Optional<Employee> employee(int id)throws SQLException{return employees().stream().filter(x->x.id()==id).findFirst();}
    public Employee createEmployee(Employee x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("INSERT INTO employees(first_name,last_name,dob) VALUES(?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS)){bind(s,x.firstName(),x.lastName());s.setDate(3,Date.valueOf(x.dob()));s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();return new Employee(k.getInt(1),x.firstName(),x.lastName(),x.dob());}}}
    public Optional<Employee> updateEmployee(int id,Employee x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("UPDATE employees SET first_name=?,last_name=?,dob=? WHERE employee_id=?")){bind(s,x.firstName(),x.lastName());s.setDate(3,Date.valueOf(x.dob()));s.setInt(4,id);if(s.executeUpdate()==0)return Optional.empty();return Optional.of(new Employee(id,x.firstName(),x.lastName(),x.dob()));}}
    public boolean deleteEmployee(int id)throws SQLException{return delete("employees","employee_id",id);}

    public List<Warehouse> warehouses() throws SQLException {List<Warehouse>o=new ArrayList<>();try(var c=database.getConnection();var s=c.prepareStatement("SELECT warehouse_id,street,city,state,pincode,capacity,manager_id FROM warehouses ORDER BY warehouse_id");var r=s.executeQuery()){while(r.next())o.add(new Warehouse(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getInt(6),(Integer)r.getObject(7)));}return o;}
    public Optional<Warehouse> warehouse(int id)throws SQLException{return warehouses().stream().filter(x->x.id()==id).findFirst();}
    public Warehouse createWarehouse(Warehouse x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("INSERT INTO warehouses(street,city,state,pincode,capacity,manager_id) VALUES(?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS)){bind(s,x.street(),x.city(),x.state(),x.pincode());s.setInt(5,x.capacity());if(x.managerId()==null)s.setNull(6,java.sql.Types.INTEGER);else s.setInt(6,x.managerId());s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();return new Warehouse(k.getInt(1),x.street(),x.city(),x.state(),x.pincode(),x.capacity(),x.managerId());}}}
    public Optional<Warehouse> updateWarehouse(int id,Warehouse x)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("UPDATE warehouses SET street=?,city=?,state=?,pincode=?,capacity=?,manager_id=? WHERE warehouse_id=?")){bind(s,x.street(),x.city(),x.state(),x.pincode());s.setInt(5,x.capacity());if(x.managerId()==null)s.setNull(6,java.sql.Types.INTEGER);else s.setInt(6,x.managerId());s.setInt(7,id);if(s.executeUpdate()==0)return Optional.empty();return Optional.of(new Warehouse(id,x.street(),x.city(),x.state(),x.pincode(),x.capacity(),x.managerId()));}}
    public boolean deleteWarehouse(int id)throws SQLException{return delete("warehouses","warehouse_id",id);}

    // ---------- contacts (multivalued attribute) ----------
    // Table/column names come from this fixed list only, never from user input.
    private static String[] contactMeta(String kind) {
        return switch (kind) {
            case "supplier" -> new String[]{"supplier_contacts", "supplier_id", "supplier_contact_id"};
            case "customer" -> new String[]{"customer_contacts", "customer_id", "customer_contact_id"};
            case "employee" -> new String[]{"employee_contacts", "employee_id", "employee_contact_id"};
            default -> throw new IllegalArgumentException("Contacts are not supported for " + kind);
        };
    }

    public List<Contact> contacts(String kind, int ownerId) throws SQLException {
        String[] m = contactMeta(kind);
        String sql = "SELECT " + m[2] + ", contact_value FROM " + m[0] + " WHERE " + m[1] + " = ? ORDER BY " + m[2];
        List<Contact> out = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setInt(1, ownerId);
            try (var r = s.executeQuery()) {
                while (r.next()) out.add(new Contact(r.getInt(1), r.getString(2)));
            }
        }
        return out;
    }

    public Contact addContact(String kind, int ownerId, String value) throws SQLException {
        String[] m = contactMeta(kind);
        String sql = "INSERT INTO " + m[0] + " (" + m[1] + ", contact_value) VALUES (?, ?)";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, ownerId);
            s.setString(2, value);
            s.executeUpdate();
            try (var k = s.getGeneratedKeys()) {
                if (!k.next()) throw new SQLException("Contact ID was not generated");
                return new Contact(k.getInt(1), value);
            }
        }
    }

    public boolean deleteContact(String kind, int ownerId, int contactId) throws SQLException {
        String[] m = contactMeta(kind);
        String sql = "DELETE FROM " + m[0] + " WHERE " + m[2] + " = ? AND " + m[1] + " = ?";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setInt(1, contactId);
            s.setInt(2, ownerId);
            return s.executeUpdate() > 0;
        }
    }

    // ---------- supplier <-> product (M:N junction table supplier_products) ----------
    public List<LinkedProduct> supplierProducts(int supplierId) throws SQLException {
        String sql = "SELECT p.product_id, p.name FROM supplier_products sp "
                + "JOIN products p ON p.product_id = sp.product_id WHERE sp.supplier_id = ? ORDER BY p.name";
        List<LinkedProduct> out = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setInt(1, supplierId);
            try (var r = s.executeQuery()) {
                while (r.next()) out.add(new LinkedProduct(r.getInt(1), r.getString(2)));
            }
        }
        return out;
    }

    public LinkedProduct linkProduct(int supplierId, int productId) throws SQLException {
        try (var c = database.getConnection()) {
            try (var s = c.prepareStatement("INSERT INTO supplier_products (supplier_id, product_id) VALUES (?, ?)")) {
                s.setInt(1, supplierId);
                s.setInt(2, productId);
                s.executeUpdate();
            }
            try (var s = c.prepareStatement("SELECT name FROM products WHERE product_id = ?")) {
                s.setInt(1, productId);
                try (var r = s.executeQuery()) {
                    return new LinkedProduct(productId, r.next() ? r.getString(1) : "");
                }
            }
        }
    }

    public boolean unlinkProduct(int supplierId, int productId) throws SQLException {
        try (var c = database.getConnection();
             var s = c.prepareStatement("DELETE FROM supplier_products WHERE supplier_id = ? AND product_id = ?")) {
            s.setInt(1, supplierId);
            s.setInt(2, productId);
            return s.executeUpdate() > 0;
        }
    }

    // ---------- employee ISA: managers / warehouse_staff ----------
    public List<Employee> managers() throws SQLException {
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, e.dob FROM employees e "
                + "JOIN managers m ON m.employee_id = e.employee_id ORDER BY e.last_name, e.first_name";
        List<Employee> out = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(sql); var r = s.executeQuery()) {
            while (r.next()) out.add(new Employee(r.getInt(1), r.getString(2), r.getString(3), r.getDate(4).toLocalDate()));
        }
        return out;
    }

    public EmployeeRole role(int employeeId) throws SQLException {
        try (var c = database.getConnection()) {
            try (var s = c.prepareStatement("SELECT department, team_size FROM managers WHERE employee_id = ?")) {
                s.setInt(1, employeeId);
                try (var r = s.executeQuery()) {
                    if (r.next()) return new EmployeeRole("MANAGER", r.getString(1), r.getInt(2), null, null);
                }
            }
            try (var s = c.prepareStatement("SELECT warehouse_id, shift_timing FROM warehouse_staff WHERE employee_id = ?")) {
                s.setInt(1, employeeId);
                try (var r = s.executeQuery()) {
                    if (r.next()) return new EmployeeRole("WAREHOUSE_STAFF", null, null, r.getInt(1), r.getString(2));
                }
            }
            return new EmployeeRole("NONE", null, null, null, null);
        }
    }

    // One transaction: the employee ends up in AT MOST ONE subtype table (disjoint + partial ISA).
    public void setRole(int employeeId, EmployeeRole r) throws SQLException {
        try (var c = database.getConnection()) {
            c.setAutoCommit(false);
            try {
                switch (r.type()) {
                    case "MANAGER" -> {
                        exec(c, "DELETE FROM warehouse_staff WHERE employee_id = ?", employeeId);
                        int teamSize = r.teamSize() == null ? 0 : r.teamSize();
                        if (exists(c, "SELECT 1 FROM managers WHERE employee_id = ?", employeeId)) {
                            try (var s = c.prepareStatement("UPDATE managers SET department = ?, team_size = ? WHERE employee_id = ?")) {
                                s.setString(1, r.department().trim());
                                s.setInt(2, teamSize);
                                s.setInt(3, employeeId);
                                s.executeUpdate();
                            }
                        } else {
                            try (var s = c.prepareStatement("INSERT INTO managers (employee_id, department, team_size) VALUES (?, ?, ?)")) {
                                s.setInt(1, employeeId);
                                s.setString(2, r.department().trim());
                                s.setInt(3, teamSize);
                                s.executeUpdate();
                            }
                        }
                    }
                    case "WAREHOUSE_STAFF" -> {
                        exec(c, "DELETE FROM managers WHERE employee_id = ?", employeeId);
                        if (exists(c, "SELECT 1 FROM warehouse_staff WHERE employee_id = ?", employeeId)) {
                            try (var s = c.prepareStatement("UPDATE warehouse_staff SET warehouse_id = ?, shift_timing = ? WHERE employee_id = ?")) {
                                s.setInt(1, r.warehouseId());
                                s.setString(2, r.shiftTiming().trim());
                                s.setInt(3, employeeId);
                                s.executeUpdate();
                            }
                        } else {
                            try (var s = c.prepareStatement("INSERT INTO warehouse_staff (employee_id, warehouse_id, shift_timing) VALUES (?, ?, ?)")) {
                                s.setInt(1, employeeId);
                                s.setInt(2, r.warehouseId());
                                s.setString(3, r.shiftTiming().trim());
                                s.executeUpdate();
                            }
                        }
                    }
                    default -> {
                        exec(c, "DELETE FROM warehouse_staff WHERE employee_id = ?", employeeId);
                        exec(c, "DELETE FROM managers WHERE employee_id = ?", employeeId);
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void exec(java.sql.Connection c, String sql, int id) throws SQLException {
        try (var s = c.prepareStatement(sql)) {
            s.setInt(1, id);
            s.executeUpdate();
        }
    }

    private static boolean exists(java.sql.Connection c, String sql, int id) throws SQLException {
        try (var s = c.prepareStatement(sql)) {
            s.setInt(1, id);
            try (var r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    private static void bind(java.sql.PreparedStatement s,String...v)throws SQLException{for(int i=0;i<v.length;i++)if(v[i]==null)s.setNull(i+1,java.sql.Types.VARCHAR);else s.setString(i+1,v[i]);}
    private boolean delete(String table,String key,int id)throws SQLException{try(var c=database.getConnection();var s=c.prepareStatement("DELETE FROM "+table+" WHERE "+key+"=?")){s.setInt(1,id);return s.executeUpdate()>0;}}
}
