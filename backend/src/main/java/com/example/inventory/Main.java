package com.example.inventory;

import com.example.inventory.config.DatabaseConfig;
import com.example.inventory.server.HttpServerApp;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();
        HttpServerApp server = new HttpServerApp(databaseConfig);
        server.start();
    }
}
