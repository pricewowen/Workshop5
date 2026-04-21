// Contributor(s): Robbie
// Main: Robbie - Login request DTO for Workshop 7 auth endpoint.

package com.sait.workshop05.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Locale;

/**
 * Request body for Workshop 7 login at {@code POST /api/v1/auth/login}. Send username or email
 * but not both. The API prefers username when both appear so this DTO keeps only one set.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDto {
    private String username;
    private String email;
    private String password;
    private boolean rememberMe;

    /**
     * Creates an empty login request payload.
     */
    public LoginRequestDto() {}

    /**
     * Builds DTO from the login field. Uses email when input contains at sign else username.
     *
     * @param principal login text entered by user.
     * @param password raw password value for authentication.
     * @return populated DTO with either {@code username} or {@code email}.
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

    /**
     * Returns username login principal when login is username-based.
     *
     * @return username login value.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username login principal.
     *
     * @param username username login value.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns email login principal when login is email-based.
     *
     * @return email login value.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email login principal.
     *
     * @param email email login value.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns raw password value sent to the auth endpoint.
     *
     * @return raw password value.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets raw password value sent to the auth endpoint.
     *
     * @param password raw password value.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns remember-me preference for login session handling.
     *
     * @return remember-me flag value.
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Sets remember-me preference for login session handling.
     *
     * @param rememberMe remember-me flag value.
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
