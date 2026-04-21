// Contributor(s): Robbie
// Main: Robbie - Log entry model for audit trail display.

package com.sait.workshop05.models;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private  String user;
    private String action;
    private String target;
    private String currentDate;

    private static final String DEFAULT_USER = "SYSTEM";
    private static String loggedInUser = null;

    /**
     * Sets the in-memory username used for later log lines when staff sign in.
     *
     * @param username staff username
     */
    public static void setLoggedInUser(String username) {
        loggedInUser = username;
    }

    /**
     * Clears the in-memory username when staff sign out.
     */
    public static void clearLoggedInUser() {
        loggedInUser = null;
    }

    /**
     * Creates one audit log line payload.
     *
     * @param action action label for the log line.
     * @param description target or description label for the log line.
     */
    public Log(String action, String description) {
        // Use the active username when present so audit rows map to staff actions.
        String user = DEFAULT_USER;

        if (loggedInUser != null && !loggedInUser.trim().isEmpty()) {
            user = loggedInUser;
        }

        // Store timestamp with local timezone so support can compare with local reports.
        Instant now = Instant.now();
        ZonedDateTime localTime = now.atZone(ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        this.user = (user != null) ? user.toUpperCase() : DEFAULT_USER;
        this.action = (action != null) ? action.toUpperCase() : "UNKNOWN_ACTION";
        this.target = (description != null) ? description.toUpperCase() : "NO DESCRIPTION";
        this.currentDate = localTime.format(formatter);
    }

    /**
     * @return formatted current date value.
     */
    public String getCurrentDate() {
        return currentDate;
    }

    /**
     * @param currentDate formatted current date value.
     */
    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * @return user label value.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user user label value.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return action label value.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action action label value.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return target label value.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target target label value.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return formatted audit line string.
     */
    @Override
    public String toString() {
        return currentDate + " | USER=" + user + " | ACTION=" + action + " | TARGET=" + target;
    }
}