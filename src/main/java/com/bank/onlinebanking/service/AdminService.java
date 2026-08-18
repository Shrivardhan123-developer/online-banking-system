package com.bank.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onlinebanking.dto.AccountResponse;
import com.bank.onlinebanking.dto.AdminStatsResponse;
import com.bank.onlinebanking.dto.AuditLogResponse;
import com.bank.onlinebanking.dto.CustomerResponse;
import com.bank.onlinebanking.dto.TransactionResponse;
import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.AuditLog;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.exception.InvalidTransactionException;
import com.bank.onlinebanking.exception.ResourceNotFoundException;
import com.bank.onlinebanking.exception.UnauthorizedOperationException;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.AuditLogRepository;
import com.bank.onlinebanking.repository.CustomerRepository;
import com.bank.onlinebanking.repository.TransactionRepository;
import com.bank.onlinebanking.util.CurrentUserService;
import com.bank.onlinebanking.util.DtoMapper;

@Service
public class AdminService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public AdminService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AuditLogRepository auditLogRepository,
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    // =====================================================
    // OVERVIEW STATISTICS
    // =====================================================

    public AdminStatsResponse getStats() {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countActive();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countActive();
        BigDecimal totalBalance = accountRepository.sumAllBalances();

        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalCustomers(totalCustomers);
        stats.setActiveCustomers(activeCustomers);
        stats.setFrozenCustomers(totalCustomers - activeCustomers);
        stats.setTotalAccounts(totalAccounts);
        stats.setActiveAccounts(activeAccounts);
        stats.setFrozenAccounts(totalAccounts - activeAccounts);
        stats.setTotalBalance(totalBalance);
        stats.setTotalTransactions(transactionRepository.count());
        stats.setTotalDeposits(transactionRepository.countDeposits());
        stats.setTotalWithdrawals(transactionRepository.countWithdrawals());
        stats.setTotalTransfers(transactionRepository.countTransfers());
        stats.setTodayDeposits(transactionRepository.countDepositsSince(todayStart));
        stats.setTodayTransfers(transactionRepository.countTransfersSince(todayStart));
        stats.setTodayDepositAmount(transactionRepository.sumDepositsSince(todayStart));

        return stats;
    }

    // =====================================================
    // CUSTOMERS (search by name / email)
    // =====================================================

    public Page<CustomerResponse> searchCustomers(String search, Pageable pageable) {

        Page<Customer> page;
        if (search == null || search.isBlank()) {
            page = customerRepository.findAll(pageable);
        } else {
            String term = search.trim();
            page = customerRepository
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            term, term, pageable);
        }

        return page.map(c -> DtoMapper.toCustomerResponse(
                c, accountRepository.findByCustomerId(c.getId())));
    }

    // =====================================================
    // ACCOUNTS (filter by number / type / status)
    // =====================================================

    public Page<AccountResponse> searchAccounts(
            String accountNumber,
            String accountType,
            String status,
            Pageable pageable) {

        return accountRepository
                .search(blankToNull(accountNumber),
                        blankToNull(accountType),
                        blankToNull(status),
                        pageable)
                .map(DtoMapper::toAccountResponse);
    }

    // =====================================================
    // TRANSACTIONS (filter by type / status / account / dates)
    // =====================================================

    public Page<TransactionResponse> searchTransactions(
            String type,
            String status,
            String accountNumber,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        return transactionRepository
                .searchAll(blankToNull(type),
                        blankToNull(status),
                        blankToNull(accountNumber),
                        fromDate,
                        toDate,
                        pageable)
                .map(DtoMapper::toTransactionResponse);
    }

    // =====================================================
    // CUSTOMER DETAILS (by id)
    // =====================================================

    public CustomerResponse getCustomerDetail(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));

        return DtoMapper.toCustomerResponse(
                customer,
                accountRepository.findByCustomerId(customer.getId()));
    }

    // =====================================================
    // ACCOUNT DETAILS (by account number)
    // =====================================================

    public AccountResponse getAccountDetail(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));

        return DtoMapper.toAccountResponse(account);
    }

    // =====================================================
    // TRANSACTIONS FOR ONE ACCOUNT (paginated, newest first)
    // Used by admin account monitoring.
    // =====================================================

    public Page<TransactionResponse> getAccountTransactions(
            String accountNumber,
            Pageable pageable) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));

        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(
                        account.getId(), pageable)
                .map(DtoMapper::toTransactionResponse);
    }

    // =====================================================
    // AUDIT LOGS (filter by action / username, paginated)
    // Views only the safe AuditLogResponse projection - never
    // passwords, hashes or other sensitive credentials.
    // =====================================================

    public Page<AuditLogResponse> getAuditLogs(
            String action,
            String username,
            Pageable pageable) {

        return auditLogRepository
                .search(blankToNull(action),
                        blankToNull(username),
                        pageable)
                .map(AuditLogResponse::from);
    }

    // =====================================================
    // CUSTOMER STATUS MANAGEMENT
    // =====================================================

    @Transactional
    public CustomerResponse updateCustomerStatus(Long id, String status) {

        String normalized = normalizeStatus(status);
        if (!"ACTIVE".equals(normalized)
                && !"SUSPENDED".equals(normalized)) {
            throw new InvalidTransactionException(
                    "Customer status must be ACTIVE or SUSPENDED");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));

        if ("ADMIN".equalsIgnoreCase(customer.getRole())) {
            throw new UnauthorizedOperationException(
                    "The admin account cannot be deactivated");
        }

        customer.setStatus(normalized);
        Customer saved = customerRepository.save(customer);

        auditService.log("ADMIN_ACTION",
                "Customer " + saved.getEmail() + " status set to " + status,
                currentUserService.getCurrentEmail());

        return DtoMapper.toCustomerResponse(
                saved, accountRepository.findByCustomerId(saved.getId()));
    }

    // =====================================================
    // ACCOUNT STATUS MANAGEMENT
    // =====================================================

    @Transactional
    public AccountResponse updateAccountStatus(Long id, String status) {

        String normalized = normalizeStatus(status);
        if (!"ACTIVE".equals(normalized)
                && !"INACTIVE".equals(normalized)) {
            throw new InvalidTransactionException(
                    "Account status must be ACTIVE or INACTIVE");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        account.setStatus(normalized);
        Account saved = accountRepository.save(account);

        auditService.log("ADMIN_ACTION",
                "Account " + saved.getAccountNumber() + " status set to " + status,
                currentUserService.getCurrentEmail());

        return DtoMapper.toAccountResponse(saved);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        return (status == null || status.isBlank())
                ? ""
                : status.trim().toUpperCase();
    }
}