package com.sait.workshop05.database;

public class UserOption {
    private final int userId;
    private final String username;

    public UserOption(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        // What shows in the ComboBox dropdown
        return userId + " - " + username;
    }
}
