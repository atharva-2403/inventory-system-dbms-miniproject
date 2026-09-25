package com.example.inventory.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final DatabaseConfig config;

    public Database(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        String driver = config.url().startsWith("jdbc:mysql:")
                ? "com.mysql.cj.jdbc.Driver"
                : "org.mariadb.jdbc.Driver";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Configured JDBC driver is not available", exception);
        }
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
