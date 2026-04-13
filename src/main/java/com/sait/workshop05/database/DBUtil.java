package com.sait.workshop05.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBUtil {

    // Look for .env.local in project root
    private static final String ENV_PATH =
            System.getProperty("user.dir") + "/.env.local";

    private static final Map<String, String> env = new HashMap<>();

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {

        try (BufferedReader reader = new BufferedReader(
                new FileReader(ENV_PATH))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {

        String url = env.get("DB_URL");
        String user = env.get("DB_USER");
        String password = env.get("DB_PASSWORD");

        if (url == null || user == null) {
            throw new RuntimeException("Missing DB settings in .env file");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
