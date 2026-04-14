package com.sait.workshop05.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Locale;

/**
 * Matches Workshop 7 {@code LoginRequest}: send <strong>either</strong> {@code username} <strong>or</strong>
 * {@code email}, not both — the API uses username first when present.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDto {
    private String username;
    private String email;
    private String password;
    private boolean rememberMe;

    public LoginRequestDto() {}

    /**
     * Builds a login body: treats input as email when it contains {@code @}, otherwise as username.
     */
    public static LoginRequestDto fromLoginPrincipal(String principal, String password) {
        LoginRequestDto dto = new LoginRequestDto();
        String p = principal.trim();
        dto.password = password;
        if (p.contains("@")) {
            dto.email = p.toLowerCase(Locale.ROOT);
        } else {
            dto.username = p;
        }
        return dto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
