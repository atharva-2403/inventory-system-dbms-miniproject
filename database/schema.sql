CREATE DATABASE IF NOT EXISTS inventory_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventory_management;

CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    street VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplier_contacts (
    supplier_contact_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    contact_value VARCHAR(150) NOT NULL,
    CONSTRAINT fk_supplier_contacts_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT uq_supplier_contact UNIQUE (supplier_id, contact_value)
);

CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_products_price CHECK (unit_price >= 0),
    CONSTRAINT chk_products_stock CHECK (quantity_in_stock >= 0),
    CONSTRAINT chk_products_reorder CHECK (reorder_level >= 0)
);

CREATE TABLE perishable_products (
    product_id INT PRIMARY KEY,
    expiry_date DATE NOT NULL,
    storage_temperature DECIMAL(6, 2) NOT NULL,
    CONSTRAINT fk_perishable_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_perishable_temperature CHECK (storage_temperature >= -100 AND storage_temperature <= 100)
);

CREATE TABLE non_perishable_products (
    product_id INT PRIMARY KEY,
    warranty_period_months INT NOT NULL,
    CONSTRAINT fk_non_perishable_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_non_perishable_warranty CHECK (warranty_period_months >= 0)
);

CREATE TABLE supplier_products (
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (supplier_id, product_id),
    CONSTRAINT fk_supplier_products_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT fk_supplier_products_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_contacts (
    employee_contact_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    contact_value VARCHAR(150) NOT NULL,
    CONSTRAINT fk_employee_contacts_employee
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT uq_employee_contact UNIQUE (employee_id, contact_value)
);

CREATE TABLE managers (
    employee_id INT PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    team_size INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_managers_employee
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT chk_manager_team_size CHECK (team_size >= 0)
);

CREATE TABLE warehouses (
    warehouse_id INT PRIMARY KEY AUTO_INCREMENT,
    street VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    capacity INT NOT NULL,
    manager_id INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_warehouses_manager
        FOREIGN KEY (manager_id) REFERENCES managers(employee_id),
    CONSTRAINT chk_warehouse_capacity CHECK (capacity >= 0)
);

CREATE TABLE warehouse_staff (
    employee_id INT PRIMARY KEY,
    warehouse_id INT NOT NULL,
    shift_timing VARCHAR(100) NOT NULL,
    CONSTRAINT fk_warehouse_staff_employee
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT fk_warehouse_staff_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
);

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    street VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_contacts (
    customer_contact_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    contact_value VARCHAR(150) NOT NULL,
    CONSTRAINT fk_customer_contacts_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT uq_customer_contact UNIQUE (customer_id, contact_value)
);

CREATE TABLE purchase_orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date DATE NOT NULL,
    status ENUM('DRAFT', 'PENDING', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    approved_by_manager_id INT,
    CONSTRAINT fk_purchase_orders_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT fk_purchase_orders_manager
        FOREIGN KEY (approved_by_manager_id) REFERENCES managers(employee_id)
);

CREATE TABLE purchase_order_details (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_purchase_details_order
        FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id),
    CONSTRAINT fk_purchase_details_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_purchase_order_product UNIQUE (order_id, product_id),
    CONSTRAINT chk_purchase_detail_quantity CHECK (quantity > 0),
    CONSTRAINT chk_purchase_detail_price CHECK (unit_price >= 0)
);

CREATE TABLE sales_orders (
    sales_order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    status ENUM('DRAFT', 'PENDING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT fk_sales_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE sales_order_details (
    sales_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    sales_order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_sales_details_order
        FOREIGN KEY (sales_order_id) REFERENCES sales_orders(sales_order_id),
    CONSTRAINT fk_sales_details_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_sales_order_product UNIQUE (sales_order_id, product_id),
    CONSTRAINT chk_sales_detail_quantity CHECK (quantity > 0),
    CONSTRAINT chk_sales_detail_price CHECK (unit_price >= 0)
);

CREATE TABLE stock_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    transaction_type ENUM('PURCHASE_RECEIPT', 'SALE', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_by_employee_id INT,
    purchase_order_id INT,
    sales_order_id INT,
    CONSTRAINT fk_stock_transactions_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_stock_transactions_employee
        FOREIGN KEY (recorded_by_employee_id) REFERENCES employees(employee_id),
    CONSTRAINT fk_stock_transactions_purchase_order
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(order_id),
    CONSTRAINT fk_stock_transactions_sales_order
        FOREIGN KEY (sales_order_id) REFERENCES sales_orders(sales_order_id),
    CONSTRAINT chk_stock_transaction_quantity CHECK (quantity <> 0)
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_supplier_products_product ON supplier_products(product_id);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_details_product ON purchase_order_details(product_id);
CREATE INDEX idx_sales_orders_customer ON sales_orders(customer_id);
CREATE INDEX idx_sales_details_product ON sales_order_details(product_id);
CREATE INDEX idx_stock_transactions_product ON stock_transactions(product_id);
CREATE INDEX idx_stock_transactions_date ON stock_transactions(transaction_date);
