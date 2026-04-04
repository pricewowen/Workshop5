package com.sait.workshop05.database;

import com.sait.workshop05.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for authentication operations
 */
public class AuthDAO {

    /**
     * Authenticate a user with email and password
     * @param email The email address
     * @param password The plain text password
     * @param role The role to authenticate as (EMPLOYEE or ADMIN)
     * @return User object if authentication successful, null otherwise
     */
    public static User authenticateByEmail(String email, String password, String role) {
        String sql = "SELECT userId, userUsername, userEmail, userPasswordHash, userRole, userCreatedAt " +
                    "FROM User WHERE userEmail = ? AND userRole = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, role.toUpperCase());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("userPasswordHash");

                // Verify password using BCrypt
                if (BCrypt.checkpw(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setUsername(rs.getString("userUsername"));
                    user.setEmail(rs.getString("userEmail"));
                    user.setPasswordHash(storedHash);
                    user.setRole(rs.getString("userRole"));

                    Timestamp timestamp = rs.getTimestamp("userCreatedAt");
                    if (timestamp != null) {
                        user.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating user by email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The plain text password
     * @param role The role to authenticate as (EMPLOYEE or ADMIN)
     * @return User object if authentication successful, null otherwise
     */
    public static User authenticate(String username, String password, String role) {
        String sql = "SELECT userId, userUsername, userEmail, userPasswordHash, userRole, userCreatedAt " +
                    "FROM User WHERE userUsername = ? AND userRole = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role.toUpperCase());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("userPasswordHash");

                // Verify password using BCrypt
                if (BCrypt.checkpw(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setUsername(rs.getString("userUsername"));
                    user.setEmail(rs.getString("userEmail"));
                    user.setPasswordHash(storedHash);
                    user.setRole(rs.getString("userRole"));

                    Timestamp timestamp = rs.getTimestamp("userCreatedAt");
                    if (timestamp != null) {
                        user.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Validate if a user session is still valid
     * @param userId The user ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateSession(int userId) {
        String sql = "SELECT userId FROM User WHERE userId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error validating session: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a user by username
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    public static User getUserByUsername(String username) {
        String sql = "SELECT userId, userUsername, userEmail, userPasswordHash, userRole, userCreatedAt " +
                    "FROM User WHERE userUsername = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("userUsername"));
                user.setEmail(rs.getString("userEmail"));
                user.setPasswordHash(rs.getString("userPasswordHash"));
                user.setRole(rs.getString("userRole"));

                Timestamp timestamp = rs.getTimestamp("userCreatedAt");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Register a new user
     * @param username The username
     * @param email The email address
     * @param password The plain text password
     * @param role The role (EMPLOYEE or ADMIN)
     * @return User object if registration successful, null otherwise
     */
    public static User registerUser(String username, String email, String password, String role) {
        String sql = "INSERT INTO User (userUsername, userEmail, userPasswordHash, userRole, userCreatedAt) " +
                    "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role.toUpperCase());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated user ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        // Return the newly created user
                        User user = new User();
                        user.setUserId(userId);
                        user.setUsername(username);
                        user.setEmail(email);
                        user.setPasswordHash(hashedPassword);
                        user.setRole(role.toUpperCase());
                        user.setCreatedAt(java.time.LocalDateTime.now());

                        System.out.println("User registered successfully: " + username);
                        return user;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get a user by email address
     * @param email The email to search for
     * @return User object if found, null otherwise
     */
    public static User getUserByEmail(String email) {
        String sql = "SELECT userId, userUsername, userEmail, userPasswordHash, userRole, userCreatedAt " +
                    "FROM User WHERE userEmail = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("userUsername"));
                user.setEmail(rs.getString("userEmail"));
                user.setPasswordHash(rs.getString("userPasswordHash"));
                user.setRole(rs.getString("userRole"));

                Timestamp timestamp = rs.getTimestamp("userCreatedAt");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Hash a password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify a password against a hash
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password to check against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
}

