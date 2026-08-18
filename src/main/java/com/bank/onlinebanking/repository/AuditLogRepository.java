package com.bank.onlinebanking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bank.onlinebanking.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select a from AuditLog a where "
            + "(:action is null or a.action like %:action%) and "
            + "(:username is null or a.username like %:username%)")
    Page<AuditLog> search(
            @Param("action") String action,
            @Param("username") String username,
            Pageable pageable);
}