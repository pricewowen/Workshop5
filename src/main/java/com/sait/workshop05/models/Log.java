package com.sait.workshop05.models;

public class Log {
    private  String user;
    private String action;
    private String description;

    public Log(String user, String action, String description) {
        this.user = user.toUpperCase();
        this.action = action.toUpperCase();
        this.description = description.toUpperCase();
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
}