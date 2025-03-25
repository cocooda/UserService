package com.vifinancenews.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlserver://svdbdemo.database.windows.net:1433;"
        + "database=UserDB;"
        + "user=imadmin@svdbdemo;"
        + "password=Password1;"
        + "encrypt=true;"
        + "trustServerCertificate=false;"
        + "hostNameInCertificate=*.database.windows.net;"
        + "loginTimeout=30;";
        
    private static final String USER = "imadmin@svdbdemo";
    private static final String PASSWORD = "Password1";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

