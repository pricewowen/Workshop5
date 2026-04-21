// Contributor(s): Robbie
// Main: Robbie - User account pick list row for linking profiles.

package com.sait.workshop05.models;

/**
 * Login account pick list row keyed by API user id with username as the visible label.
 */
public class UserOption {
    /** API user id (UUID string). */
    private final String userId;
    private final String username;

    /**
     * Creates one user option row for account-link pickers.
     */
    public UserOption(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * Returns API user id.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns username label for display.
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
