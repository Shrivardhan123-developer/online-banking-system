package com.bank.onlinebanking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.LoginRequest;
import com.bank.onlinebanking.dto.LoginResponse;
import com.bank.onlinebanking.dto.MessageResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.service.AuthService;
import com.bank.onlinebanking.service.CustomerService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CustomerService customerService;

    public AuthController(
            AuthService authService,
            CustomerService customerService) {

        this.authService = authService;
        this.customerService = customerService;
    }

    // =====================================================
    // REGISTER CUSTOMER
    // POST /api/auth/register
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @RequestBody Customer customer) {

        customerService.registerCustomer(customer);

        return new ResponseEntity<>(
                new MessageResponse(
                        "Customer registered successfully"),
                HttpStatus.CREATED
        );
    }

    // =====================================================
    // LOGIN
    // POST /api/auth/login
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}