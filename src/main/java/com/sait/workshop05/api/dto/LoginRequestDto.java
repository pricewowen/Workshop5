package com.sait.workshop05.api.dto;

public class LoginRequestDto {
    private String username;
    private String email;
    private String password;

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.username = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
