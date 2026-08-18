package com.bank.onlinebanking.dto;

import java.math.BigDecimal;

/**
 * Aggregated banking statistics for the admin dashboard.
 */
public class AdminStatsResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long frozenCustomers;
    private long totalAccounts;
    private long activeAccounts;
    private long frozenAccounts;
    private BigDecimal totalBalance;
    private long totalTransactions;
    private long totalDeposits;
    private long totalWithdrawals;
    private long totalTransfers;
    private long todayDeposits;
    private long todayTransfers;
    private BigDecimal todayDepositAmount;

    public AdminStatsResponse() {
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public long getFrozenCustomers() {
        return frozenCustomers;
    }

    public void setFrozenCustomers(long frozenCustomers) {
        this.frozenCustomers = frozenCustomers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(long activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public long getFrozenAccounts() {
        return frozenAccounts;
    }

    public void setFrozenAccounts(long frozenAccounts) {
        this.frozenAccounts = frozenAccounts;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(long totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public long getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(long totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public long getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(long totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public long getTodayDeposits() {
        return todayDeposits;
    }

    public void setTodayDeposits(long todayDeposits) {
        this.todayDeposits = todayDeposits;
    }

    public long getTodayTransfers() {
        return todayTransfers;
    }

    public void setTodayTransfers(long todayTransfers) {
        this.todayTransfers = todayTransfers;
    }

    public BigDecimal getTodayDepositAmount() {
        return todayDepositAmount;
    }

    public void setTodayDepositAmount(BigDecimal todayDepositAmount) {
        this.todayDepositAmount = todayDepositAmount;
    }
}