package com.bank.onlinebanking.dto;

import java.math.BigDecimal;
import java.util.List;

import com.bank.onlinebanking.entity.Customer;

/**
 * Aggregated data shown on the customer dashboard. All numbers come from
 * real backend queries, never from client input.
 */
public class DashboardResponse {

    private CustomerResponse customer;
    private List<AccountResponse> accounts;
    private BigDecimal totalBalance;
    private long totalAccounts;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfers;
    private List<TransactionResponse> recentTransactions;

    public DashboardResponse() {
    }

    public static DashboardResponse of(
            Customer customer,
            CustomerResponse customerResponse,
            List<AccountResponse> accounts,
            BigDecimal totalBalance,
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            BigDecimal totalTransfers,
            List<TransactionResponse> recentTransactions) {

        DashboardResponse dto = new DashboardResponse();
        dto.customer = customerResponse;
        dto.accounts = accounts;
        dto.totalBalance = totalBalance;
        dto.totalAccounts = accounts == null ? 0 : accounts.size();
        dto.totalDeposits = totalDeposits == null ? BigDecimal.ZERO : totalDeposits;
        dto.totalWithdrawals = totalWithdrawals == null ? BigDecimal.ZERO : totalWithdrawals;
        dto.totalTransfers = totalTransfers == null ? BigDecimal.ZERO : totalTransfers;
        dto.recentTransactions = recentTransactions;
        return dto;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public BigDecimal getTotalTransfers() {
        return totalTransfers;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }
}