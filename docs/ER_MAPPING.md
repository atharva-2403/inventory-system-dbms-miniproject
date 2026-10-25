# Inventory Management System ER Mapping

## Confirmed implementation decisions

| Area | Decision |
|---|---|
| Backend | Java 21, Maven, JDK `HttpServer`, Jackson for JSON |
| Supplier/product relationship | Many-to-many through `supplier_products` |
| Stock model | Global stock quantity on `products`; warehouses are modeled as storage locations but do not own separate quantities in version 1 |
| Order detail identity | Surrogate detail IDs with a unique `(order_id, product_id)` rule |
| Employee subtypes | Disjoint and partial: an employee may have neither subtype, but may not be both |
| Stock transaction types | `PURCHASE_RECEIPT`, `SALE`, and `ADJUSTMENT` |
| Delete policy | Restrict deletion of referenced records; the API returns a clear conflict/error |
| Derived values | Product stock value, order totals, employee age, and warehouse occupancy are calculated rather than stored |

## Entity-to-table mapping

| ER entity | Table | Primary key | Notes |
|---|---|---|---|
| Supplier | `suppliers` | `supplier_id` | Address is decomposed into columns |
| Supplier contact | `supplier_contacts` | `supplier_contact_id` | Separate table preserves the multivalued contact attribute |
| Purchase_Order | `purchase_orders` | `order_id` | Each order belongs to one supplier and may be approved by one manager |
| Order_Detail | `purchase_order_details` | `order_detail_id` | Surrogate key; `(order_id, product_id)` is unique |
| Category | `categories` | `category_id` | Category name is unique |
| Product | `products` | `product_id` | Global inventory quantity is stored here |
| Perishable_Product | `perishable_products` | `product_id` | Primary key is also a foreign key to `products` |
| Non_Perishable_Product | `non_perishable_products` | `product_id` | Primary key is also a foreign key to `products` |
| Supplier/Product association | `supplier_products` | `(supplier_id, product_id)` | Many-to-many `supplies` relationship |
| Warehouse | `warehouses` | `warehouse_id` | Location is decomposed into columns |
| Employee | `employees` | `employee_id` | Name and contact are decomposed/mapped separately |
| Employee contact | `employee_contacts` | `employee_contact_id` | Separate table preserves the multivalued contact attribute |
| Manager | `managers` | `employee_id` | Employee subtype |
| Warehouse_Staff | `warehouse_staff` | `employee_id` | Employee subtype |
| Customer | `customers` | `customer_id` | Name and address are decomposed |
| Customer contact | `customer_contacts` | `customer_contact_id` | Separate table preserves the multivalued contact attribute |
| Sales_Order | `sales_orders` | `sales_order_id` | Each order belongs to one customer |
| Sales_Order_Detail | `sales_order_details` | `sales_detail_id` | Surrogate key; `(sales_order_id, product_id)` is unique |
| StockTransaction | `stock_transactions` | `transaction_id` | Immutable audit records for stock changes |

## Relationship mapping

| Relationship | Relational mapping | Cardinality/assumption |
|---|---|---|
| `supplies` | `supplier_products(supplier_id, product_id)` | Many-to-many |
| `classifies` | `products.category_id` | One category to many products; every product has one category |
| `stores` | `warehouses` is represented independently; no warehouse stock junction in version 1 | The diagram does not provide enough detail to allocate global stock across warehouses |
| `placed by` | `purchase_orders.supplier_id` | One supplier can have many purchase orders; each purchase order has one supplier |
| `approves` | `purchase_orders.approved_by_manager_id` | A manager may approve many purchase orders; approval is modeled against the purchase order |
| `contains_order` / `appears_in_order` | `purchase_order_details.order_id` and `.product_id` | One purchase order contains one or more products |
| `manages` | `warehouses.manager_id` | One manager may manage many warehouses; each warehouse has at most one manager |
| `works_in` | `warehouse_staff.warehouse_id` | One warehouse may have many staff members; each warehouse staff member works in at most one warehouse |
| `involved in` | `stock_transactions.product_id` | One product may have many stock transactions |
| `records` | `stock_transactions.recorded_by_employee_id` | One employee may record many transactions; the relationship is nullable for system/import adjustments |
| `appears_in_sales_order` / `contains_sales` | `sales_order_details.sales_order_id` and `.product_id` | One sales order contains one or more products |
| `places` | `sales_orders.customer_id` | One customer may place many sales orders; each sales order has one customer |

## Subtype rules

### Product

`products` is the supertype. A product should have exactly one subtype row in the application: either `perishable_products` or `non_perishable_products`. MySQL does not enforce this cross-table exclusivity directly, so the service layer must validate it.

### Employee

`employees` is the supertype. `managers` and `warehouse_staff` are disjoint and partial. The service layer must reject creating/updating an employee into both subtype tables. An employee may have no subtype row.

## Derived values

- Product stock value: `unit_price * quantity_in_stock`.
- Purchase order total: sum of detail `quantity * unit_price`.
- Sales order total: sum of detail `quantity * unit_price`.
- Employee age: calculated from `dob`.
- Warehouse current occupancy: not persisted because the selected global-stock model does not define a reliable occupancy unit.

## Ambiguities and implementation assumptions

1. The supplied specification transcribes relationship names but does not provide reliable cardinality marks. The cardinalities above are the simplest normalized interpretation and should be checked against the original diagram/instructor.
2. Global product stock is used for version 1. If the instructor expects stock per warehouse, introduce `warehouse_products` and move stock quantity there before implementing order workflows.
3. The weak order-detail entities use surrogate IDs for easier CRUD/API handling, while uniqueness on parent order plus product preserves one line per product.
4. The approval relationship is attached to purchase orders rather than suppliers because approval is operationally associated with an order.
5. Deletion is restricted for referenced records. Historical stock transactions are not edited or deleted through the normal application.

