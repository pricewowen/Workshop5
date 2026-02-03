package com.sait.workshop05.models;

import java.time.LocalDateTime;

public class Log {
    private  String user;
    private String action;
    private String description;
    private LocalDateTime currentDate;

    public Log(String user, String action, String description) {
        LocalDateTime currentTime = LocalDateTime.now();

//        String currentTimeString = (currentTime.getYear() + "-" + currentTime.getMonthValue() + "-"
//                + currentTime.getDayOfMonth() + "|" + currentTime.getHour() + ":" + currentTime.getMinute()
//                + ":" + currentTime.getSecond());

        this.user = user.toUpperCase();
        this.action = action.toUpperCase();
        this.description = description.toUpperCase();
        this.currentDate = currentTime;
    }

    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDateTime currentDate) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "USER=" + user + " | " + action + "=" + description + " | " + currentDate;
    }
}