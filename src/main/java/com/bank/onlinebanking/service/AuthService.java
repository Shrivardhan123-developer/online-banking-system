package com.bank.onlinebanking.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.bank.onlinebanking.dto.LoginRequest;
import com.bank.onlinebanking.dto.LoginResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.exception.UnauthorizedOperationException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            AuthenticationManager authenticationManager,
            CustomerService customerService,
            JwtService jwtService,
            AuditService auditService) {

        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    // =====================================================
    // LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        if (request == null || request.getEmail() == null
                || request.getPassword() == null) {
            throw new UnauthorizedOperationException(
                    "Email and password are required");
        }

        // Throws BadCredentialsException on failure -> mapped to 401.
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()));

        if (!authentication.isAuthenticated()) {
            throw new UnauthorizedOperationException(
                    "Invalid email or password");
        }

        Customer customer =
                customerService.getCustomerByEmail(request.getEmail());

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            throw new UnauthorizedOperationException(
                    "Your account has been suspended. Contact support.");
        }

        String token = jwtService.generateToken(customer.getEmail());

        auditService.log("LOGIN", "User logged in", customer.getEmail());

        return new LoginResponse(
                "Login successful",
                token,
                customer.getId(),
                customer.getEmail(),
                customer.getFullName(),
                customer.getRole(),
                customer.getStatus()
        );
    }
}