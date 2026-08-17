package com.bank.onlinebanking.dto;

import java.util.List;

public class CustomerResponse {

    private Long id;

    private String email;

    private String fullName;

    private String phone;

    private String role;

    private String status;

    private List<AccountResponse> accounts;


    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public CustomerResponse() {
    }


    // =====================================================
    // PARAMETERIZED CONSTRUCTOR
    // =====================================================

    public CustomerResponse(
            Long id,
            String email,
            String fullName,
            String phone,
            String role,
            String status,
            List<AccountResponse> accounts) {

        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.accounts = accounts;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAccounts(List<AccountResponse> accounts) {
        this.accounts = accounts;
    }
}
