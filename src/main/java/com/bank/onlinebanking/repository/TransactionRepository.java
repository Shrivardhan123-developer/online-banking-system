package com.bank.onlinebanking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.onlinebanking.entity.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // =====================================================
    // GET TRANSACTIONS BY ACCOUNT ID
    // Used by TransactionService
    // =====================================================

    List<Transaction>
    findByAccountIdOrderByTransactionDateDesc(
            Long accountId
    );

    // =====================================================
    // GET TRANSACTIONS BY ACCOUNT NUMBER
    // Used by TransactionController
    // =====================================================

    List<Transaction>
    findByAccountAccountNumberOrderByTransactionDateDesc(
            String accountNumber
    );

    // =====================================================
    // PAGINATED HISTORY FOR A CUSTOMER WITH FILTERS
    // Filters: type (DEPOSIT/WITHDRAW/TRANSFER_IN/TRANSFER_OUT),
    // status, date range, owner customer.
    // =====================================================

    @Query("select t from Transaction t where "
            + "t.account.customer.id = :customerId and "
            + "(:type is null or t.type = :type) and "
            + "(:status is null or t.status = :status) and "
            + "(:fromDate is null or t.transactionDate >= :fromDate) and "
            + "(:toDate is null or t.transactionDate <= :toDate)")
    Page<Transaction> searchForCustomer(
            @Param("customerId") Long customerId,
            @Param("type") String type,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // =====================================================
    // PAGINATED HISTORY FOR ONE ACCOUNT OF A CUSTOMER
    // =====================================================

    @Query("select t from Transaction t where "
            + "t.account.accountNumber = :accountNumber and "
            + "(:type is null or t.type = :type) and "
            + "(:status is null or t.status = :status)")
    Page<Transaction> searchForAccount(
            @Param("accountNumber") String accountNumber,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable);

    // =====================================================
    // ADMIN SEARCH ACROSS ALL ACCOUNTS
    // =====================================================

    @Query("select t from Transaction t where "
            + "(:type is null or t.type = :type) and "
            + "(:status is null or t.status = :status) and "
            + "(:accountNumber is null or t.account.accountNumber like %:accountNumber%) and "
            + "(:fromDate is null or t.transactionDate >= :fromDate) and "
            + "(:toDate is null or t.transactionDate <= :toDate)")
    Page<Transaction> searchAll(
            @Param("type") String type,
            @Param("status") String status,
            @Param("accountNumber") String accountNumber,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // =====================================================
    // ADMIN STATISTICS
    // =====================================================

    @Query("select count(t) from Transaction t where t.type = 'DEPOSIT'")
    long countDeposits();

    @Query("select count(t) from Transaction t where t.type = 'WITHDRAW'")
    long countWithdrawals();

    @Query("select count(t) from Transaction t where t.type like 'TRANSFER%'")
    long countTransfers();

    @Query("select count(t) from Transaction t where t.type = 'TRANSFER_OUT' and "
            + "t.transactionDate >= :fromDate")
    long countTransfersSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("select count(t) from Transaction t where t.type = 'DEPOSIT' and "
            + "t.transactionDate >= :fromDate")
    long countDepositsSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t "
            + "where t.type = 'DEPOSIT' and t.transactionDate >= :fromDate")
    java.math.BigDecimal sumDepositsSince(@Param("fromDate") LocalDateTime fromDate);

    // =====================================================
    // CUSTOMER DASHBOARD AGGREGATES
    // =====================================================

    @Query("select coalesce(sum(t.amount), 0) from Transaction t "
            + "where t.account.customer.id = :customerId and t.type = 'DEPOSIT'")
    java.math.BigDecimal sumDepositsForCustomer(@Param("customerId") Long customerId);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t "
            + "where t.account.customer.id = :customerId and t.type = 'WITHDRAW'")
    java.math.BigDecimal sumWithdrawalsForCustomer(@Param("customerId") Long customerId);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t "
            + "where t.account.customer.id = :customerId and t.type = 'TRANSFER_OUT'")
    java.math.BigDecimal sumTransfersOutForCustomer(@Param("customerId") Long customerId);
}