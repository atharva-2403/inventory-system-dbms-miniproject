USE inventory_management;

INSERT INTO categories (category_name, description) VALUES
    ('Beverages', 'Packaged drinks and beverage products'),
    ('Office Supplies', 'Common supplies used in offices'),
    ('Cleaning', 'Cleaning and sanitation products');

INSERT INTO suppliers (name, street, city, state, pincode) VALUES
    ('FreshMart Distributors', '12 Market Road', 'Pune', 'Maharashtra', '411001'),
    ('OfficeWorld Suppliers', '45 Station Road', 'Mumbai', 'Maharashtra', '400001');

INSERT INTO supplier_contacts (supplier_id, contact_value) VALUES
    (1, 'freshmart@example.com'),
    (1, '+91-9000000001'),
    (2, 'officeworld@example.com');

INSERT INTO products (
    category_id,
    name,
    unit_price,
    quantity_in_stock,
    reorder_level
) VALUES
    (1, 'Bottled Water 1L', 20.00, 25, 10),
    (2, 'A4 Copy Paper Pack', 320.00, 10, 5),
    (3, 'Liquid Hand Wash 500ml', 145.00, 5, 8);

INSERT INTO perishable_products (
    product_id,
    expiry_date,
    storage_temperature
) VALUES
    (1, '2027-10-23', 20.00);

INSERT INTO non_perishable_products (
    product_id,
    warranty_period_months
) VALUES
    (2, 0),
    (3, 0);

INSERT INTO supplier_products (supplier_id, product_id) VALUES
    (1, 1),
    (1, 3),
    (2, 2);

INSERT INTO employees (first_name, last_name, dob) VALUES
    ('Aarav', 'Kulkarni', '1985-04-12'),
    ('Neha', 'Patil', '1998-09-21');

INSERT INTO employee_contacts (employee_id, contact_value) VALUES
    (1, '+91-9000000011'),
    (2, '+91-9000000012');

INSERT INTO managers (employee_id, department, team_size) VALUES
    (1, 'Operations', 4);

INSERT INTO warehouses (
    street,
    city,
    state,
    pincode,
    capacity,
    manager_id
) VALUES
    ('88 Industrial Estate', 'Pune', 'Maharashtra', '411019', 1000, 1);

INSERT INTO warehouse_staff (employee_id, warehouse_id, shift_timing) VALUES
    (2, 1, '09:00-17:00');

INSERT INTO customers (
    first_name,
    last_name,
    street,
    city,
    state,
    pincode
) VALUES
    ('Riya', 'Sharma', '7 College Road', 'Pune', 'Maharashtra', '411005'),
    ('Vikram', 'Desai', '22 Lake View', 'Nashik', 'Maharashtra', '422001');

INSERT INTO customer_contacts (customer_id, contact_value) VALUES
    (1, 'riya.sharma@example.com'),
    (1, '+91-9000000021'),
    (2, '+91-9000000022');

INSERT INTO purchase_orders (
    supplier_id,
    order_date,
    status,
    approved_by_manager_id
) VALUES
    (1, '2026-10-20', 'PENDING', 1);

INSERT INTO purchase_order_details (
    order_id,
    product_id,
    quantity,
    unit_price
) VALUES
    (1, 1, 50, 18.00),
    (1, 3, 20, 120.00);

INSERT INTO sales_orders (
    customer_id,
    order_date,
    status
) VALUES
    (1, '2026-10-21', 'PENDING');

INSERT INTO sales_order_details (
    sales_order_id,
    product_id,
    quantity,
    unit_price
) VALUES
    (1, 1, 3, 25.00),
    (1, 2, 2, 350.00);

INSERT INTO stock_transactions (
    product_id,
    transaction_type,
    quantity,
    recorded_by_employee_id
) VALUES
    (1, 'ADJUSTMENT', 25, 1),
    (2, 'ADJUSTMENT', 10, 1),
    (3, 'ADJUSTMENT', 5, 1);
