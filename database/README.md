# Database setup

## Requirements

- MySQL 8.x
- A local MySQL user with permission to create/use the `inventory_management` database

## Initialize a clean database

From this directory, run:

```bash
mysql -u root -p < schema.sql
mysql -u root -p inventory_management < seed.sql
```

Replace `root` with the local project database user when appropriate.

The schema creates the database if it does not exist. The seed script assumes
the schema is empty and should not be run repeatedly without clearing the
database first.

## Verify the seed data

```sql
USE inventory_management;

SELECT COUNT(*) AS categories FROM categories;
SELECT COUNT(*) AS products FROM products;
SELECT COUNT(*) AS suppliers FROM suppliers;
SELECT COUNT(*) AS employees FROM employees;
SELECT COUNT(*) AS customers FROM customers;
SELECT COUNT(*) AS purchase_orders FROM purchase_orders;
SELECT COUNT(*) AS sales_orders FROM sales_orders;
SELECT COUNT(*) AS stock_transactions FROM stock_transactions;

SELECT
    p.product_id,
    p.name,
    p.quantity_in_stock,
    p.unit_price * p.quantity_in_stock AS stock_value
FROM products p
ORDER BY p.product_id;
```

Expected product stock is:

| Product | Quantity |
|---|---:|
| Bottled Water 1L | 25 |
| A4 Copy Paper Pack | 10 |
| Liquid Hand Wash 500ml | 5 |

The purchase and sales orders are intentionally left in `PENDING` status so
the later backend workflow can demonstrate receiving and completing orders.
