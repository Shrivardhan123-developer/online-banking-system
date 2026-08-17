package com.bank.onlinebanking.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.onlinebanking.entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    // =====================================================
    // FIND CUSTOMER BY EMAIL
    // =====================================================

    Optional<Customer> findByEmail(String email);

    // =====================================================
    // CHECK EMAIL
    // =====================================================

    boolean existsByEmail(String email);

    // =====================================================
    // ADMIN SEARCH (by name / email, paginated)
    // =====================================================

    Page<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    // =====================================================
    // COUNT ACTIVE CUSTOMERS
    // =====================================================

    @Query("select count(c) from Customer c where c.status = 'ACTIVE'")
    long countActive();

    // =====================================================
    // CHECK STATUS BY ID (used by user details loading)
    // =====================================================

    @Query("select c.status from Customer c where c.id = :id")
    Optional<String> findStatusById(@Param("id") Long id);
}