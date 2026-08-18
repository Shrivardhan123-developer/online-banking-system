package com.bank.onlinebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Used by admins to activate / deactivate a customer or account.
 * Accepted statuses: ACTIVE, INACTIVE (accounts) and ACTIVE, SUSPENDED (users).
 */
public class UpdateStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE|SUSPENDED",
            message = "Status must be ACTIVE, INACTIVE or SUSPENDED")
    private String status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}