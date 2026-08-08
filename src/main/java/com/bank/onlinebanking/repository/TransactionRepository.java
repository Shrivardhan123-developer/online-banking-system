package com.bank.onlinebanking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.onlinebanking.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
}