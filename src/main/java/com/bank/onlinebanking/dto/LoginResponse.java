package com.bank.onlinebanking.dto;

public class LoginResponse {

    private String message;
    private String token;
    private Long customerId;
    private String email;
    private String fullName;
    private String role;
    private String status;

    public LoginResponse() {
    }

    public LoginResponse(
            String message,
            String token,
            Long customerId,
            String email,
            String fullName,
            String role,
            String status) {

        this.message = message;
        this.token = token;
        this.customerId = customerId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
