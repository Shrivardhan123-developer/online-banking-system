package com.bank.onlinebanking.dto;

import java.time.LocalDateTime;

import com.bank.onlinebanking.entity.AuditLog;

/**
 * Safe projection of an AuditLog row for the admin audit-log viewer.
 * Never contains passwords, hashes, tokens or other secrets.
 */
public class AuditLogResponse {

    private Long id;
    private String action;
    private String description;
    private String username;
    private LocalDateTime createdAt;

    public AuditLogResponse() {
    }

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.id = log.getId();
        dto.action = log.getAction();
        dto.description = log.getDescription();
        dto.username = log.getUsername();
        dto.createdAt = log.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}