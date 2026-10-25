# Initial project setup

The database foundation and Java backend health endpoint are now available.
The React frontend will be added in a later vertical slice.

## Database

1. Install MySQL 8.x.
2. Run `database/schema.sql`.
3. Run `database/seed.sql`.
4. Follow `database/README.md` for verification queries.

## Java backend

Requirements:

- Java 21
- Maven 3.9+
- Node.js 20+ and npm

Set the database connection variables in the shell before starting the server:

```bash
export DB_URL='jdbc:mariadb://localhost:3306/inventory_management'
export DB_USER='inventory_app'
export DB_PASSWORD='your_local_password'
export SERVER_PORT='8080'
```

Start the backend:

```bash
cd backend
mvn compile exec:java
```

Verify the server and database connection:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{"status":"ok","database":"connected"}
```

Stop the server with `Ctrl+C`.

## Frontend

Install dependencies and start Vite:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite proxy forwards `/api` requests to the
Java server on port `8080`.

## Category vertical slice

The first complete feature is category CRUD:

- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

The backend validates names, uses prepared JDBC statements, blocks deletion of
categories referenced by products, and returns structured JSON errors.

## MySQL compatibility

The backend includes both MariaDB and MySQL Connector/J drivers. MariaDB users
should use a `jdbc:mariadb://...` URL; MySQL users should use a
`jdbc:mysql://...` URL. The driver is selected from the URL, so teammates can
use their database without changing Java source code.

## Confirmed implementation choices

- Java 21
- Maven backend
- JDK `HttpServer` with Jackson JSON handling
- React + TypeScript + Vite frontend
- MySQL with direct JDBC
- Global product stock in version 1
- Restricted deletion of referenced records
- MariaDB and MySQL-compatible JDBC configuration

## Important assumption

The current schema does not allocate quantities to individual warehouses.
If the original ER diagram or instructor requires warehouse-specific stock,
the schema must be revised to add a `warehouse_products` table before order
workflow implementation.
