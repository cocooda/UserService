package com.vifinancenews.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
