package com.sait.workshop05.models;

import java.time.LocalDateTime;

public class Log {
    private  String user;
    private String action;
    private String target;
    private String currentDate;

    public Log(String user, String action, String description) {
        LocalDateTime currentTime = LocalDateTime.now();

        // formats the date and time to a more human-readable format
        String currentTimeString = (currentTime.getYear() + "-" + currentTime.getMonthValue() + "-"
                + currentTime.getDayOfMonth() + " " + currentTime.getHour() + ":" + currentTime.getMinute()
                + ":" + currentTime.getSecond());

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