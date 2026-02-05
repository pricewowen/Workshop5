package com.sait.workshop05.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Log {
    private  String user;
    private String action;
    private String target;
    private String currentDate;

    private static String loggedInUser = null;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * Sets the username of the logged-in user called when user logs in
     * @param username username of the logged-in user
     */
    public static void setLoggedInUser(String username) {
        loggedInUser = username;
    }

    /**
     * Clears the logged-in user. Called when user logs out
     */
    public static void clearLoggedInUser() {
        loggedInUser = null;
    }

    public Log(String action, String description) {
        // get the logged-in username
        String user = "SYSTEM";

        if (loggedInUser != null && !loggedInUser.trim().isEmpty()) {
            user = loggedInUser;
        }

        // gets the current time in ISO_8601/RFC 3339
        String currentTimeString = Instant.now().toString();

        this.user = user.toUpperCase();
        this.action = action.toUpperCase();
        this.target = description.toUpperCase();
        this.currentDate = currentTimeString;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return currentDate + " | USER=" + user + " | ACTION=" + action + " | TARGET=" + target;
    }
}