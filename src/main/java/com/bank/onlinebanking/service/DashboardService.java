package com.bank.onlinebanking.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.onlinebanking.dto.AccountResponse;
import com.bank.onlinebanking.dto.DashboardResponse;
import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.TransactionRepository;
import com.bank.onlinebanking.util.DtoMapper;

/**
 * Builds the real dashboard data for the currently authenticated customer.
 * Every number is computed from the database, never from client input.
 */
@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public DashboardService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionService transactionService) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    public DashboardResponse buildDashboard(Customer customer) {

        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());
        List<AccountResponse> accountResponses = DtoMapper.toAccountResponses(accounts);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeposits =
                transactionRepository.sumDepositsForCustomer(customer.getId());

        BigDecimal totalWithdrawals =
                transactionRepository.sumWithdrawalsForCustomer(customer.getId());

        BigDecimal totalTransfers =
                transactionRepository.sumTransfersOutForCustomer(customer.getId());

        List<com.bank.onlinebanking.dto.TransactionResponse> recent =
                transactionService.getRecentForCustomer(customer.getId(), 6);

        return DashboardResponse.of(
                customer,
                DtoMapper.toCustomerResponse(customer, accounts),
                accountResponses,
                totalBalance,
                totalDeposits,
                totalWithdrawals,
                totalTransfers,
                recent
        );
    }
}