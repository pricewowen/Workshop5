package com.sait.workshop05.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String SERVER = "localhost\\SQLEXPRESS";
    private static final int PORT = 1433;
    private static final String DATABASE = "BakeryEcommerce";

    private static final String URL = "jdbc:sqlserver://" + SERVER + ";" +
            "databaseName=" + DATABASE + ";" +
            "integratedSecurity=true;" +
            "encrypt=true;" + "trustServerCertificate=true;";


    public static Connection getConnection() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Database connection successful");
        } catch (SQLException e) {
            System.out.println("Problem connecting to the database");
            throw new RuntimeException("Problem connecting to database: " + e.getMessage());
        }
        return conn;
    }
}
