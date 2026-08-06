package com.bank.onlinebanking.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.CustomerResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // =========================
    // REGISTER CUSTOMER
    // =========================

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> registerCustomer(
            @RequestBody Customer customer) {

        Customer savedCustomer =
                customerService.registerCustomer(customer);

        CustomerResponse response = convertToResponse(savedCustomer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // GET CUSTOMER BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long id) {

        Customer customer =
                customerService.getCustomerById(id);

        CustomerResponse response =
                convertToResponse(customer);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ALL CUSTOMERS
    // =========================

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

        List<CustomerResponse> customers =
                customerService.getAllCustomers()
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(customers);
    }

    // =========================
    // DELETE CUSTOMER
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(
            @PathVariable Long id) {

        customerService.deleteCustomer(id);

        return ResponseEntity.ok(
                "Customer deleted successfully"
        );
    }

    // =========================
    // ENTITY -> DTO
    // =========================

    private CustomerResponse convertToResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFullName(),
                customer.getPhone()
        );
    }
}