# Inventory Management System

A small academic inventory management application built with React,
TypeScript, Java 21, JDBC, and MySQL-compatible databases.

## Current status

Implemented:

- MySQL/MariaDB database schema and seed data
- Java 21 Maven backend using JDK `HttpServer`
- Direct JDBC access with prepared statements
- JSON API with structured errors and CORS
- `/api/health` endpoint
- Complete category CRUD vertical slice
- Product CRUD with subtype fields, stock value, and low-stock labels
- Supplier, warehouse, customer, and employee list/create screens
- Purchase and sales order creation screens
- Atomic purchase receipt and sales completion workflows
- Read-only stock transaction history
- React/Vite navigation across all current features

## Technology requirements

- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- MariaDB 10.6+ or MySQL 8.x
- `curl` for optional API verification

## Project structure

```text
inventory-management-system/
├── backend/       Java HTTP API, services, DAOs, and JDBC configuration
├── frontend/      React + TypeScript + Vite application
├── database/      schema.sql, seed.sql, and database instructions
└── docs/          ER mapping, setup decisions, and test plan
```

## 1. Clone and enter the project

```bash
git clone <repository-url>
cd inventory-management-system
```

If you received the project as a folder rather than through Git, simply enter
the project directory.

## 2. Create the database

The same SQL files work with both MariaDB and MySQL.

### MariaDB

If your MariaDB `root` account uses socket authentication:

```bash
sudo mariadb < database/schema.sql
sudo mariadb inventory_management < database/seed.sql
```

If your root account uses password authentication:

```bash
mariadb -u root -p < database/schema.sql
mariadb -u root -p inventory_management < database/seed.sql
```

### MySQL

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p inventory_management < database/seed.sql
```

## 3. Create an application database user

Do not use the database root account from the Java application. Create a
dedicated user with access only to this project database.

### MariaDB

Open the administrative client:

```bash
sudo mariadb
```

For a new user:

```sql
CREATE USER 'inventory_app'@'localhost' IDENTIFIED BY 'replace_with_password';
GRANT ALL PRIVILEGES ON inventory_management.* TO 'inventory_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

If the user already exists:

```sql
ALTER USER 'inventory_app'@'localhost' IDENTIFIED BY 'replace_with_password';
GRANT ALL PRIVILEGES ON inventory_management.* TO 'inventory_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Test the login:

```bash
mariadb -u inventory_app -p inventory_management
```

### MySQL

Open the administrative client:

```bash
mysql -u root -p
```

For a new user:

```sql
CREATE USER 'inventory_app'@'localhost' IDENTIFIED BY 'replace_with_password';
GRANT ALL PRIVILEGES ON inventory_management.* TO 'inventory_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

If the user already exists:

```sql
ALTER USER 'inventory_app'@'localhost' IDENTIFIED BY 'replace_with_password';
GRANT ALL PRIVILEGES ON inventory_management.* TO 'inventory_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Test the login:

```bash
mysql -u inventory_app -p inventory_management
```

## 4. Verify the seed data

MariaDB:

```bash
mariadb -u inventory_app -p inventory_management
```

MySQL:

```bash
mysql -u inventory_app -p inventory_management
```

Run:

```sql
SELECT COUNT(*) AS products FROM products;
SELECT COUNT(*) AS categories FROM categories;
SELECT COUNT(*) AS purchase_orders FROM purchase_orders;
SELECT COUNT(*) AS sales_orders FROM sales_orders;
SELECT COUNT(*) AS stock_transactions FROM stock_transactions;
EXIT;
```

Expected counts:

```text
products: 3
categories: 3
purchase_orders: 1
sales_orders: 1
stock_transactions: 3
```

## 5. Configure and run the backend

Open a terminal in the backend directory:

```bash
cd backend
```

Set the database variables in that same terminal. Environment variables do not
automatically carry over to a new terminal.

### MariaDB

```bash
export DB_URL='jdbc:mariadb://localhost:3306/inventory_management'
export DB_USER='inventory_app'
export DB_PASSWORD='your_application_password'
export SERVER_PORT='8080'
```

### MySQL

```bash
export DB_URL='jdbc:mysql://localhost:3306/inventory_management'
export DB_USER='inventory_app'
export DB_PASSWORD='your_application_password'
export SERVER_PORT='8080'
```

Start the backend:

```bash
mvn compile exec:java
```

Expected startup message:

```text
Inventory API listening on http://localhost:8080
```

Keep this terminal running.

## 6. Verify the backend

Open a second terminal:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{"status":"ok","database":"connected"}
```

Check categories:

```bash
curl http://localhost:8080/api/categories
```

## 7. Run the frontend

Open a third terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the URL shown by Vite, normally:

```text
http://localhost:5173
```

The frontend forwards `/api` requests to the backend at
`http://localhost:8080`.

## 8. Test category CRUD

Use the frontend to:

1. View the seeded categories.
2. Add a category.
3. Edit the category.
4. Delete the category.
5. Try deleting a category referenced by a product and verify that the
   application shows a clear error.

The API endpoints are:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Check backend and database connectivity |
| GET | `/api/categories` | List categories |
| GET | `/api/categories/{id}` | Fetch one category |
| POST | `/api/categories` | Create a category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete an unreferenced category |
| GET/POST/PUT/DELETE | `/api/products` | Product CRUD |
| GET/POST/PUT/DELETE | `/api/suppliers` | Supplier CRUD |
| GET/POST/PUT/DELETE | `/api/warehouses` | Warehouse CRUD |
| GET/POST/PUT/DELETE | `/api/customers` | Customer CRUD |
| GET/POST/PUT/DELETE | `/api/employees` | Employee CRUD |
| GET/POST | `/api/purchase-orders` | Purchase order list/create |
| POST | `/api/purchase-orders/{id}/receive` | Receive purchase atomically |
| GET/POST | `/api/sales-orders` | Sales order list/create |
| POST | `/api/sales-orders/{id}/complete` | Complete sale atomically |
| GET | `/api/stock-transactions` | Read-only stock audit history |

Example create request:

```bash
curl -X POST http://localhost:8080/api/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"Electronics","description":"Electronic products"}'
```

## 9. Build checks

Backend:

```bash
cd backend
mvn clean compile
```

Frontend:

```bash
cd frontend
npm run build
```

## 10. Automated and manual testing

See `docs/TEST_PLAN.md` for backend test commands, MariaDB/MySQL smoke tests,
and the complete purchase/sales demonstration checklist.

## Troubleshooting

### `DB_USER environment variable is required`

Set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the same terminal where you run
`mvn exec:java`.

### MariaDB root login returns `ERROR 1698`

Use socket authentication:

```bash
sudo mariadb
```

Do not use `mariadb -u root -p` unless a root password was explicitly
configured.

### `Access denied for user inventory_app`

Reset the application user through an administrative MariaDB/MySQL session,
then grant access again as shown above.

### Frontend cannot connect to the API

Ensure the Java backend is running on port `8080` before starting Vite.

### Duplicate category error

Category names are unique. Use a different name or update the existing row.

## Security notes

- Never commit real database passwords.
- Never put database credentials in frontend code.
- Use a dedicated database user for the application.
- The sample commands use placeholder passwords; replace them locally.

## Design documentation

- `docs/ER_MAPPING.md` contains the confirmed ER assumptions.
- `docs/SETUP.md` contains phase-specific setup notes.
- `database/README.md` contains database-only instructions.
