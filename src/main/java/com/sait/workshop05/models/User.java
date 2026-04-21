// Contributor(s): Robbie
// Main: Robbie - Login user stub for session compatibility.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Login user stub with JavaFX properties for session and compatibility with legacy integer user ids.
 */
public class User {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty email;
    private final StringProperty passwordHash;
    private final StringProperty role;
    private final ObjectProperty<LocalDateTime> createdAt;

    /**
     * Initializes JavaFX properties used by login session state.
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
     * Convenience constructor for mapped login responses.
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

    /** Returns user id value. */
    public int getUserId() {
        return userId.get();
    }

    /** Returns JavaFX property wrapper for user id. */
    public IntegerProperty userIdProperty() {
        return userId;
    }

    /** Sets user id value. */
    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    /** Returns username value. */
    public String getUsername() {
        return username.get();
    }

    /** Returns JavaFX property wrapper for username. */
    public StringProperty usernameProperty() {
        return username;
    }

    /** Sets username value. */
    public void setUsername(String username) {
        this.username.set(username);
    }

    /** Returns email value. */
    public String getEmail() {
        return email.get();
    }

    /** Returns JavaFX property wrapper for email. */
    public StringProperty emailProperty() {
        return email;
    }

    /** Sets email value. */
    public void setEmail(String email) {
        this.email.set(email);
    }

    /** Returns password hash value. */
    public String getPasswordHash() {
        return passwordHash.get();
    }

    /** Returns JavaFX property wrapper for password hash. */
    public StringProperty passwordHashProperty() {
        return passwordHash;
    }

    /** Sets password hash value. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash.set(passwordHash);
    }

    /** Returns role value. */
    public String getRole() {
        return role.get();
    }

    /** Returns JavaFX property wrapper for role. */
    public StringProperty roleProperty() {
        return role;
    }

    /** Sets role value. */
    public void setRole(String role) {
        this.role.set(role);
    }

    /** Returns creation timestamp value. */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /** Returns JavaFX property wrapper for creation timestamp. */
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /** Sets creation timestamp value. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * @return debug-friendly summary string for the user row.
     */
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

