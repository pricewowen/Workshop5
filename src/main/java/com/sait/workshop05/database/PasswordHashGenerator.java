package com.sait.workshop05.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class to generate BCrypt hashed passwords for testing
 * Run this class to generate password hashes for test users
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        System.out.println("=== Password Hash Generator ===\n");

        // Generate hashes for common test passwords
        String[] passwords = {"admin123", "emp123", "cust123", "manager123", "customer123"};

        for (String password : passwords) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println();
        }

        System.out.println("=== Verification Test ===\n");

        // Test verification
        String testPassword = "admin123";
        String testHash = BCrypt.hashpw(testPassword, BCrypt.gensalt());
        boolean matches = BCrypt.checkpw(testPassword, testHash);

        System.out.println("Test Password: " + testPassword);
        System.out.println("Test Hash: " + testHash);
        System.out.println("Verification Result: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));
    }
}

