package com.bank.onlinebanking.service;

import org.springframework.stereotype.Service;

import com.bank.onlinebanking.entity.AuditLog;
import com.bank.onlinebanking.repository.AuditLogRepository;

/**
 * Records important banking and administrative operations.
 * Never logs passwords or other sensitive credentials.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String description, String username) {
        try {
            auditLogRepository.save(new AuditLog(action, description, username));
        } catch (Exception e) {
            // Audit logging must never break a banking operation.
        }
    }
}