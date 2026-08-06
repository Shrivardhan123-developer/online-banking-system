package com.bank.onlinebanking.service;

import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account createAccount(Long customerId, String accountType) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    private String generateAccountNumber() {

        Random random = new Random();

        long number = 1000000000L + random.nextLong(9000000000L);

        return String.valueOf(number);
    }
}