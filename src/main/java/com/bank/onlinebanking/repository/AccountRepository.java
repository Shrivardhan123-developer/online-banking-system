package com.bank.onlinebanking.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bank.onlinebanking.entity.Account;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find account using account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Find all accounts belonging to a customer
    List<Account> findByCustomerId(Long customerId);

    // Paginated accounts of a customer
    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    // Paginated accounts of a customer by status
    Page<Account> findByCustomerIdAndStatus(
            Long customerId, String status, Pageable pageable);

    // =====================================================
    // PESSIMISTIC-WRITE LOCKED LOOKUP FOR SAFE FUND MOVEMENT
    // Prevents concurrent withdrawals/transfers from overdrawing.
    // =====================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber);

    // =====================================================
    // ADMIN SEARCH (accountNumber / type / status, paginated)
    // =====================================================

    @Query("select a from Account a where "
            + "(:accountNumber is null or a.accountNumber like %:accountNumber%) and "
            + "(:accountType is null or a.accountType = :accountType) and "
            + "(:status is null or a.status = :status)")
    Page<Account> search(
            @Param("accountNumber") String accountNumber,
            @Param("accountType") String accountType,
            @Param("status") String status,
            Pageable pageable);

    // =====================================================
    // ADMIN STATISTICS
    // =====================================================

    @Query("select count(a) from Account a where a.status = 'ACTIVE'")
    long countActive();

    @Query("select coalesce(sum(a.balance), 0) from Account a")
    BigDecimal sumAllBalances();
}