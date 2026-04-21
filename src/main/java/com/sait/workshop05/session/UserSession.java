// Contributor(s): Robbie
// Main: Robbie - In-memory JWT role and profile hints for one signed-in staff user.

package com.sait.workshop05.session;

import com.sait.workshop05.models.Log;
import com.sait.workshop05.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory session for the signed-in staff user JWT and profile hints.
 * One global instance matches a single desktop stage. Clear on logout before reuse.
 */
public class UserSession {
    private static UserSession instance;

    private User currentUser;
    private boolean isAuthenticated;
    private String userRole;
    private LocalDateTime loginTime;
    private String jwtToken;

    // Principal id string from login. Chat and REST filters use it as the user id.
    private String apiUserId;

    // Employee dashboard uses profile id plus bakery ids from me and me or bakeries calls.
    private String employeeProfileId;
    private List<Integer> accessibleBakeryIds;  // Empty list blocks bakery-scoped dashboard charts.

    // Optional display fields for the sidebar avatar and initials.
    private String profilePhotoUrl;
    private String profileFirstName;
    private String profileLastName;

    private UserSession() {
        this.isAuthenticated = false;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    /**
     * Returns singleton desktop session instance.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Starts a session from authenticated user and JWT token values.
     */
    public void createSession(User user, String jwtToken) {
        this.currentUser = user;
        this.jwtToken = jwtToken;
        Log.setLoggedInUser(user.getUsername());
        this.userRole = user.getRole();
        this.isAuthenticated = true;
        this.loginTime = LocalDateTime.now();
        this.apiUserId = null;

        this.employeeProfileId = null;
        this.accessibleBakeryIds = new ArrayList<>();
    }

    // Trims empty strings so the UI can fall back to username-based initials.
    /**
     * Sets optional profile display hints used by sidebar avatar UI.
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

    /**
     * Returns profile photo URL when available.
     */
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    /**
     * Returns profile first name hint.
     */
    public String getProfileFirstName() {
        return profileFirstName;
    }

    /**
     * Returns profile last name hint.
     */
    public String getProfileLastName() {
        return profileLastName;
    }

    /**
     * Clears all session values during logout.
     */
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

    /**
     * Returns current JWT token.
     */
    public String getJwtToken() {
        return jwtToken;
    }

    /**
     * Sets API principal id used for chat and scoped API calls.
     */
    public void setApiUserId(String apiUserId) {
        this.apiUserId = apiUserId;
    }

    /**
     * Returns API principal id.
     */
    public String getApiUserId() {
        return apiUserId;
    }

    /**
     * Returns whether a user is currently authenticated.
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Returns current signed-in user model.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns current user role label.
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Returns login timestamp for the active session.
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Returns true when active session role is ADMIN.
     */
    public boolean isAdmin() {
        return isAuthenticated && userRole != null && userRole.equalsIgnoreCase("ADMIN");
    }

    /**
     * Returns true when active session role is EMPLOYEE.
     */
    public boolean isEmployee() {
        return isAuthenticated && userRole != null && userRole.equalsIgnoreCase("EMPLOYEE");
    }

    // When bakeryIds is empty the dashboard should not assume any bakery scope.
    /**
     * Sets employee analytics scope values for bakery-filtered dashboards.
     */
    public void setEmployeeAnalyticsAccess(String employeeProfileId, List<Integer> bakeryIds) {
        this.employeeProfileId = employeeProfileId;
        this.accessibleBakeryIds = (bakeryIds == null) ? new ArrayList<>() : new ArrayList<>(bakeryIds);
    }

    /**
     * Returns employee profile id used for analytics scoping.
     */
    public String getEmployeeProfileId() {
        return employeeProfileId;
    }

    /**
     * Returns read-only bakery id scope for analytics views.
     */
    public List<Integer> getAccessibleBakeryIds() {
        return Collections.unmodifiableList(accessibleBakeryIds);
    }

}