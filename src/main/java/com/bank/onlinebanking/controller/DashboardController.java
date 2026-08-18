package com.bank.onlinebanking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.DashboardResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.service.DashboardService;
import com.bank.onlinebanking.util.CurrentUserService;

/**
 * Customer dashboard endpoint. The dashboard is always built for the
 * currently authenticated customer resolved from the JWT security
 * context - a client-supplied id is never trusted.
 *
 *   GET /api/dashboard
 *
 * Security: this endpoint falls under AuthenticatedAccess /
 *api/** and therefore requires a valid JWT (any authenticated
 * customer or admin can view their own dashboard).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardController(
            DashboardService dashboardService,
            CurrentUserService currentUserService) {

        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        Customer current = currentUserService.getCurrentCustomer();

        return ResponseEntity.ok(
                dashboardService.buildDashboard(current));
    }
}