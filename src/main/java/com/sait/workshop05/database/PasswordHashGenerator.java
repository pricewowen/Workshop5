// src/main/java/com/sait/workshop05/database/PasswordHashGenerator.java
package com.sait.workshop05.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class to generate BCrypt hashed passwords for testing.
 *
 * Usage options:
 * 1) Run with no args -> generates hashes for a default list (includes ava123).
 * 2) Run with args -> hashes each arg, e.g.:
 *    PasswordHashGenerator ava123 admin123
 *
 * NOTE: BCrypt produces a different hash each time even for the same password
 * because it uses a random salt. This is expected.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        System.out.println("=== Password Hash Generator (BCrypt) ===\n");

        String[] passwords;
        if (args != null && args.length > 0) {
            passwords = args;
        } else {
            // Default list (includes the one you want)
            passwords = new String[] {
                    "ava123",
                    "admin123",
                    "emp123",
                    "manager123"
            };
        }

        for (String password : passwords) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println();
        }

        // Quick self-check
        System.out.println("=== Verification Self-Test ===\n");
        String testPassword = passwords[0];
        String testHash = BCrypt.hashpw(testPassword, BCrypt.gensalt(10));
        boolean matches = BCrypt.checkpw(testPassword, testHash);

        System.out.println("Test Password: " + testPassword);
        System.out.println("Test Hash: " + testHash);
        System.out.println("Verification Result: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));
    }
}