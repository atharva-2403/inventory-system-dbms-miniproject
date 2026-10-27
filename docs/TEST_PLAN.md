# Test plan

## Automated backend tests

Run from `backend/`:

```bash
mvn test
```

The current tests cover:

- Product subtype validation
- Invalid order details
- Duplicate products in an order
- Invalid employee IDs for stock workflows
- Receive/complete status transitions
- Master-data validation

## Build checks

Backend:

```bash
cd backend
mvn clean test
```

Frontend:

```bash
cd frontend
npm run build
```

## MariaDB/MySQL smoke test

Start the backend with the appropriate JDBC URL, then run:

```bash
curl --fail http://localhost:8080/api/health
curl --fail http://localhost:8080/api/categories
curl --fail http://localhost:8080/api/products
curl --fail http://localhost:8080/api/suppliers
curl --fail http://localhost:8080/api/warehouses
curl --fail http://localhost:8080/api/customers
curl --fail http://localhost:8080/api/employees
curl --fail http://localhost:8080/api/purchase-orders
curl --fail http://localhost:8080/api/sales-orders
curl --fail http://localhost:8080/api/stock-transactions
```

Use the MariaDB URL for MariaDB:

```text
jdbc:mariadb://localhost:3306/inventory_management
```

Use the MySQL URL for MySQL:

```text
jdbc:mysql://localhost:3306/inventory_management
```

## Manual workflow scenarios

1. Create a category and product.
2. Verify a product at or below its reorder level shows `Low stock`.
3. Create a purchase order and receive it once.
4. Verify product stock increases and a `PURCHASE_RECEIPT` transaction appears.
5. Try receiving the same purchase order again; it must fail.
6. Create a sales order and complete it.
7. Verify product stock decreases and a `SALE` transaction appears.
8. Try completing the same sales order again; it must fail.
9. Try a sale exceeding available stock; it must fail without changing stock.
10. Try deleting a referenced category or product; it must be blocked with a
    clear error.
