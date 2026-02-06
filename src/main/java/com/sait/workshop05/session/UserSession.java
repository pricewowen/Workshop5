package com.sait.workshop05.session;

import com.sait.workshop05.models.User;

import java.time.LocalDateTime;

/**
 * Singleton class to manage user session throughout the application
 */
public class UserSession {
    private static UserSession instance;

    private User currentUser;
    private boolean isAuthenticated;
    private String userRole;
    private LocalDateTime loginTime;

    private UserSession() {
        this.isAuthenticated = false;
    }

    /**
     * Get the singleton instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Create a new session for a user
     */
    public void createSession(User user) {
        this.currentUser = user;
        this.userRole = user.getRole();
        this.isAuthenticated = true;
        this.loginTime = LocalDateTime.now();
        System.out.println("Session created for user: " + user.getUsername() + " with role: " + userRole);
    }

    /**
     * Clear the current session (logout)
     */
    public void clearSession() {
        this.currentUser = null;
        this.userRole = null;
        this.isAuthenticated = false;
        this.loginTime = null;
        System.out.println("Session cleared");
    }

    /**
     * Check if a user is currently authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Get the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get the current user's role
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Get the login time
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Check if the current user is an employee or admin
     */
    public boolean isEmployeeOrAdmin() {
        return isAuthenticated && (userRole != null &&
               (userRole.equalsIgnoreCase("EMPLOYEE") || userRole.equalsIgnoreCase("ADMIN")));
    }

    /**
     * Check if the current user is a customer
     */
    public boolean isCustomer() {
        return isAuthenticated && (userRole != null && userRole.equalsIgnoreCase("CUSTOMER"));
    }
}

