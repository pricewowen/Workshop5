package com.sait.workshop05.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Generate SQL INSERT statements with BCrypt hashed passwords
 * Copy the output and paste into your SQL client
 */
public class GenerateTestUserSQL {

    public static void main(String[] args) {
        System.out.println("=== Generate Test Users SQL with BCrypt Hashes ===\n");
        System.out.println("Copy the SQL below and run it in your MySQL client:\n");
        System.out.println("----------------------------------------\n");

        // Generate the complete SQL
        System.out.println("USE BakeryEcommerce;\n");
        System.out.println("-- Clear existing test users (optional)");
        System.out.println("-- DELETE FROM `User` WHERE userUsername IN ('admin', 'employee1', 'customer1', 'manager', 'johndoe');\n");

        // Test users with passwords
        String[][] users = {
            {"admin", "admin@peelingood.com", "admin123", "ADMIN"},
            {"employee1", "employee1@peelingood.com", "emp123", "EMPLOYEE"},
            {"customer1", "customer1@example.com", "cust123", "CUSTOMER"},
            {"manager", "manager@peelingood.com", "manager123", "EMPLOYEE"},
            {"johndoe", "john.doe@example.com", "customer123", "CUSTOMER"}
        };

        System.out.println("-- Test Users (Generated: " + java.time.LocalDateTime.now() + ")");
        System.out.println("-- Credentials for testing:");
        for (String[] user : users) {
            System.out.println("-- Username: " + user[0] + " | Password: " + user[2] + " | Role: " + user[3]);
        }
        System.out.println();

        for (String[] user : users) {
            String username = user[0];
            String email = user[1];
            String plainPassword = user[2];
            String role = user[3];

            // Generate BCrypt hash
            String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

            System.out.println("-- " + role + ": " + username + " (password: " + plainPassword + ")");
            System.out.println("INSERT INTO `User` (userUsername, userEmail, userPasswordHash, userRole, userCreatedAt)");
            System.out.println("VALUES ('" + username + "', '" + email + "', ");
            System.out.println("        '" + hash + "', ");
            System.out.println("        '" + role + "', NOW());");
            System.out.println();
        }

        System.out.println("-- Verify inserted users");
        System.out.println("SELECT userId, userUsername, userEmail, userRole, userCreatedAt FROM `User` ORDER BY userId;");

        System.out.println("\n----------------------------------------");
        System.out.println("\n✓ SQL generation complete!");
        System.out.println("\nNOTE: Each time you run this, the hashes will be different (BCrypt uses random salt).");
        System.out.println("This is normal and expected behavior.\n");
    }
}

