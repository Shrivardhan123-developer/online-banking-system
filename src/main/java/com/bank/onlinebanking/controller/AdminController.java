package com.bank.onlinebanking.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.AccountResponse;
import com.bank.onlinebanking.dto.AdminStatsResponse;
import com.bank.onlinebanking.dto.AuditLogResponse;
import com.bank.onlinebanking.dto.CustomerResponse;
import com.bank.onlinebanking.dto.PageResponse;
import com.bank.onlinebanking.dto.TransactionResponse;
import com.bank.onlinebanking.dto.UpdateStatusRequest;
import com.bank.onlinebanking.service.AdminService;

import jakarta.validation.Valid;

/**
 * Administrative API. Every endpoint is guarded by the security layer
 * (ROLE_ADMIN / /api/admin/**) and delegates all business logic to
 * AdminService. Controllers stay thin and never touch repositories.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // =====================================================
    // OVERVIEW STATISTICS
    // GET /api/admin/stats
    // =====================================================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // =====================================================
    // CUSTOMERS
    // GET /api/admin/customers?search=&page=&size=
    // =====================================================

    @GetMapping("/customers")
    public ResponseEntity<PageResponse<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("id"));

        return ResponseEntity.ok(
                PageResponse.from(
                        adminService.searchCustomers(search, pageable)));
    }
// =====================================================
    // CUSTOMER DETAILS
    // GET /api/admin/customers/{id}
    // =====================================================

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCustomerDetail(id));
    }

    // =====================================================
    // CUSTOMER STATUS MANAGEMENT
    // PUT /api/admin/customers/{id}/status
    // Body: UpdateStatusRequest { "status": "ACTIVE" | "SUSPENDED" }
    // =====================================================

    @PutMapping("/customers/{id}/status")
    public ResponseEntity<CustomerResponse> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {

        return ResponseEntity.ok(
                adminService.updateCustomerStatus(id, request.getStatus()));
    }

    // =====================================================
    // ACCOUNTS
    // GET /api/admin/accounts?accountNumber=&accountType=&status=
    // =====================================================

    @GetMapping("/accounts")
    public ResponseEntity<PageResponse<AccountResponse>> getAccounts(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("id"));

        return ResponseEntity.ok(
                PageResponse.from(
                        adminService.searchAccounts(
                                accountNumber, accountType, status, pageable)));
    }

    // =====================================================
    // ACCOUNT DETAILS
    // GET /api/admin/accounts/{accountNumber}
    // =====================================================

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(
                adminService.getAccountDetail(accountNumber));
    }
// =====================================================
    // ACCOUNT TRANSACTIONS
    // GET /api/admin/accounts/{accountNumber}/transactions
    // =====================================================

    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<PageResponse<TransactionResponse>> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "transactionDate"));

        return ResponseEntity.ok(
                PageResponse.from(
                        adminService.getAccountTransactions(
                                accountNumber, pageable)));
    }

    // =====================================================
    // ACCOUNT STATUS MANAGEMENT
    // PUT /api/admin/accounts/{id}/status
    // Body: UpdateStatusRequest { "status": "ACTIVE" | "INACTIVE" }
    // =====================================================

    @PutMapping("/accounts/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {

        return ResponseEntity.ok(
                adminService.updateAccountStatus(id, request.getStatus()));
    }

    // =====================================================
    // AUDIT LOGS
    // GET /api/admin/audit-logs?action=&username=&page=&size=
    // =====================================================

    @GetMapping("/audit-logs")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(
                PageResponse.from(
                        adminService.getAuditLogs(action, username, pageable)));
    }
}