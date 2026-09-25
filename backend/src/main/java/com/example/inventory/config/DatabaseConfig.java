package com.example.inventory.config;

public record DatabaseConfig(String url, String user, String password, int serverPort) {
    public static DatabaseConfig fromEnvironment() {
        String url = valueOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/inventory_management");
        String user = requiredValue("DB_USER");
        String password = requiredValue("DB_PASSWORD");
        int port = parsePort(valueOrDefault("SERVER_PORT", "8080"));
        return new DatabaseConfig(url, user, password, port);
    }

    private static String requiredValue(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable is required");
        }
        return value;
    }

    private static String valueOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("SERVER_PORT must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("SERVER_PORT must be a number", exception);
        }
    }
}
