package com.sait.workshop05.models;

public class UserOption {
    /** API user id (UUID string). */
    private final String userId;
    private final String username;

    public UserOption(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
