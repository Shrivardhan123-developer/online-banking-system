package com.bank.onlinebanking.dto;

public class LoginResponse {

    private String message;
    private String email;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String message, String email, String token) {
        this.message = message;
        this.email = email;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}