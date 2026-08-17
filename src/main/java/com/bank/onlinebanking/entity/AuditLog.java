package com.bank.onlinebanking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Audit trail for important banking and administration operations.
 * Never stores passwords or sensitive credentials.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** E.g. LOGIN, LOGOUT, DEPOSIT, WITHDRAW, TRANSFER, ACCOUNT_CREATED ... */
    @Column(nullable = false, length = 40)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Email of the principal performing the action (if known). */
    @Column(length = 120)
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(String action, String description, String username) {
        this.action = action;
        this.description = description;
        this.username = username;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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
}