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
    private String jwtToken;

    /** Logged-in user's UUID from {@code AuthResponse.userId} (API principal). */
    private String apiUserId;

    // Analytics gating for EMPLOYEE (employee profile id from API; bakery scope for charts)
    private String employeeProfileId;
    private List<Integer> accessibleBakeryIds;  // empty means no scope

    /** From login {@code AuthResponse} — for sidebar avatar / initials. */
    private String profilePhotoUrl;
    private String profileFirstName;
    private String profileLastName;

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

    public void createSession(User user, String jwtToken) {
        this.currentUser = user;
        this.jwtToken = jwtToken;
        Log.setLoggedInUser(user.getUsername());
        this.userRole = user.getRole();
        this.isAuthenticated = true;
        this.loginTime = LocalDateTime.now();
        this.apiUserId = null;

        // Reset employee analytics info on new login
        this.employeeProfileId = null;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    /**
     * Stores display hints for the main UI (photo URL from object storage, employee names when present).
     */
    public void setProfileDisplayHints(String firstName, String lastName, String profilePhotoPath) {
        this.profileFirstName = firstName != null ? firstName.trim() : null;
        if (this.profileFirstName != null && this.profileFirstName.isEmpty()) {
            this.profileFirstName = null;
        }
        this.profileLastName = lastName != null ? lastName.trim() : null;
        if (this.profileLastName != null && this.profileLastName.isEmpty()) {
            this.profileLastName = null;
        }
        this.profilePhotoUrl = profilePhotoPath != null && !profilePhotoPath.isBlank()
                ? profilePhotoPath.trim()
                : null;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getProfileFirstName() {
        return profileFirstName;
    }

    public String getProfileLastName() {
        return profileLastName;
    }

    public void clearSession() {
        this.currentUser = null;
        this.jwtToken = null;
        Log.clearLoggedInUser();
        this.userRole = null;
        this.isAuthenticated = false;
        this.loginTime = null;

        this.employeeProfileId = null;
        this.accessibleBakeryIds = new ArrayList<>();
        this.profilePhotoUrl = null;
        this.profileFirstName = null;
        this.profileLastName = null;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setApiUserId(String apiUserId) {
        this.apiUserId = apiUserId;
    }

    public String getApiUserId() {
        return apiUserId;
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
     * If employeeProfileId is null or bakeryIds is empty, analytics should be disabled.
     */
    public void setEmployeeAnalyticsAccess(String employeeProfileId, List<Integer> bakeryIds) {
        this.employeeProfileId = employeeProfileId;
        this.accessibleBakeryIds = (bakeryIds == null) ? new ArrayList<>() : new ArrayList<>(bakeryIds);
    }

    public String getEmployeeProfileId() {
        return employeeProfileId;
    }

    public List<Integer> getAccessibleBakeryIds() {
        return Collections.unmodifiableList(accessibleBakeryIds);
    }

}