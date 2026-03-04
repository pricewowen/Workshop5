package com.sait.workshop05.session;

import com.sait.workshop05.models.Log;
import com.sait.workshop05.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton class to manage user session throughout the application.
 * Supports Admin and Employee roles only.
 */
public class UserSession {
    private static UserSession instance;

    private User currentUser;
    private boolean isAuthenticated;
    private String userRole;
    private LocalDateTime loginTime;

    // Analytics gating for EMPLOYEE
    private Integer employeeId;                 // null if not a real Employee row
    private List<Integer> accessibleBakeryIds;  // empty means no scope

    private UserSession() {
        this.isAuthenticated = false;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void createSession(User user) {
        this.currentUser = user;
        Log.setLoggedInUser(user.getUsername());
        this.userRole = user.getRole();
        this.isAuthenticated = true;
        this.loginTime = LocalDateTime.now();

        // Reset employee analytics info on new login
        this.employeeId = null;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    public void clearSession() {
        this.currentUser = null;
        Log.clearLoggedInUser();
        this.userRole = null;
        this.isAuthenticated = false;
        this.loginTime = null;

        this.employeeId = null;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUserRole() {
        return userRole;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public boolean isAdmin() {
        return isAuthenticated && userRole != null && userRole.equalsIgnoreCase("ADMIN");
    }

    public boolean isEmployee() {
        return isAuthenticated && userRole != null && userRole.equalsIgnoreCase("EMPLOYEE");
    }

    /**
     * Called at login for EMPLOYEE accounts.
     * If employeeId is null or bakeryIds is empty, analytics should be disabled.
     */
    public void setEmployeeAnalyticsAccess(Integer employeeId, List<Integer> bakeryIds) {
        this.employeeId = employeeId;
        this.accessibleBakeryIds = (bakeryIds == null) ? new ArrayList<>() : new ArrayList<>(bakeryIds);
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public List<Integer> getAccessibleBakeryIds() {
        return Collections.unmodifiableList(accessibleBakeryIds);
    }

    /**
     * Policy:
     * - Admin: always true
     * - Employee: only true if linked to an Employee row AND has bakery scope
     */
    public boolean canAccessAnalytics() {
        if (isAdmin()) return true;
        if (!isEmployee()) return false;

        return employeeId != null && !accessibleBakeryIds.isEmpty();
    }
}