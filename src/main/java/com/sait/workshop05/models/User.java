package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * User model representing a user in the system.
 * Can be either an Employee, Admin, or Customer.
 */
public class User {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty email;
    private final StringProperty passwordHash;
    private final StringProperty role;
    private final ObjectProperty<LocalDateTime> createdAt;

    /**
     * Default constructor - initializes all properties
     */
    public User() {
        this.userId = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.passwordHash = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
    }

    /**
     * Full constructor
     */
    public User(int userId, String username, String email, String passwordHash, String role, LocalDateTime createdAt) {
        this();
        setUserId(userId);
        setUsername(username);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(role);
        setCreatedAt(createdAt);
    }

    // Getters and Setters
    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPasswordHash() {
        return passwordHash.get();
    }

    public StringProperty passwordHashProperty() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash.set(passwordHash);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId.get() +
                ", username='" + username.get() + '\'' +
                ", email='" + email.get() + '\'' +
                ", role='" + role.get() + '\'' +
                ", createdAt=" + createdAt.get() +
                '}';
    }
}

