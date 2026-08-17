package com.bank.onlinebanking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.ChangePasswordRequest;
import com.bank.onlinebanking.dto.CustomerResponse;
import com.bank.onlinebanking.dto.MessageResponse;
import com.bank.onlinebanking.dto.UpdateProfileRequest;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.exception.UnauthorizedOperationException;
import com.bank.onlinebanking.service.AccountService;
import com.bank.onlinebanking.service.CustomerService;
import com.bank.onlinebanking.util.DtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerController(
            CustomerService customerService,
            AccountService accountService) {

        this.customerService = customerService;
        this.accountService = accountService;
    }

    // =====================================================
    // CREATE CUSTOMER
    // POST /api/customers
    // =====================================================

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody Customer customer) {

        Customer savedCustomer =
                customerService.registerCustomer(customer);

        CustomerResponse response =
                buildCustomerResponse(savedCustomer);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    // =====================================================
    // GET OWN PROFILE
    // GET /api/customers/me
    // The profile is resolved from the authenticated JWT,
    // never from a client-supplied id.
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile() {

        Customer current =
                customerService.getCurrentCustomer();

        return ResponseEntity.ok(
                buildCustomerResponse(current));
    }

    // =====================================================
    // GET CUSTOMER DETAILS
    // GET /api/customers/{customerId}
    // =====================================================

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable Long customerId) {

        Customer current =
                customerService.getCurrentCustomer();

        Customer customer =
                customerService.getCustomerById(customerId);

        // Customers may only view their own profile; admins may
        // view any profile. Never trust a customer-supplied id.
        if (!"ADMIN".equalsIgnoreCase(current.getRole())
                && !current.getId().equals(customer.getId())) {
            throw new UnauthorizedOperationException(
                    "You do not have access to this customer's data");
        }

        CustomerResponse response =
                buildCustomerResponse(customer);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE OWN PROFILE
    // PUT /api/customers/me
    // Only the authenticated customer's own profile is updated.
    // =====================================================

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        Customer updated =
                customerService.updateCurrentProfile(request);

        CustomerResponse response =
                buildCustomerResponse(updated);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE PROFILE BY CUSTOMER ID
    // PUT /api/customers/{customerId}
    // Customers may only update their own profile; admins may
    // update any profile. Never trust a customer-supplied id.
    // =====================================================

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateProfileRequest request) {

        Customer current =
                customerService.getCurrentCustomer();

        Customer target =
                customerService.getCustomerById(customerId);

        if (!"ADMIN".equalsIgnoreCase(current.getRole())
                && !current.getId().equals(target.getId())) {
            throw new UnauthorizedOperationException(
                    "You do not have access to update this customer's profile");
        }

        Customer updated =
                customerService.updateProfile(target.getId(), request);

        CustomerResponse response =
                buildCustomerResponse(updated);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // CHANGE OWN PASSWORD
    // PUT /api/customers/me/password
    // Only the authenticated customer's password can change.
    // =====================================================

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        customerService.changePassword(request);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Password changed successfully"));
    }

    // =====================================================
    // BUILD CUSTOMER RESPONSE
    // Uses the shared DtoMapper (which never serialises the
    // password) and resolves accounts through the service layer
    // so the controller performs no direct repository access.
    // =====================================================

    private CustomerResponse buildCustomerResponse(
            Customer customer) {

        return DtoMapper.toCustomerResponse(
                customer,
                accountService.getAccountsForCustomer(
                        customer.getId()));
    }
}