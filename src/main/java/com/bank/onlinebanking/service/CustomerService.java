package com.bank.onlinebanking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onlinebanking.dto.ChangePasswordRequest;
import com.bank.onlinebanking.dto.RegisterRequest;
import com.bank.onlinebanking.dto.UpdateProfileRequest;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.exception.DuplicateResourceException;
import com.bank.onlinebanking.exception.InvalidTransactionException;
import com.bank.onlinebanking.exception.ResourceNotFoundException;
import com.bank.onlinebanking.repository.CustomerRepository;
import com.bank.onlinebanking.util.CurrentUserService;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public CustomerService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            AccountService accountService,
            AuditService auditService,
            CurrentUserService currentUserService) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    // =====================================================
    // REGISTER (from validated RegisterRequest)
    // =====================================================

    @Transactional
    public Customer registerCustomer(RegisterRequest request) {

        if (request == null) {
            throw new InvalidTransactionException("Registration details are required");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidTransactionException(
                    "Password and confirm password do not match");
        }

        checkEmailAvailable(request.getEmail());

        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(normaliseEmail(request.getEmail()));
        customer.setPhone(request.getPhone().trim());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole("CUSTOMER");
        customer.setStatus("ACTIVE");

        Customer saved = customerRepository.save(customer);

        // A new customer gets a default savings account so the app is
        // immediately usable after registration.
        accountService.createAccount(saved, "SAVINGS");

        auditService.log("REGISTER", "New customer registered", saved.getEmail());

        return saved;
    }
// =====================================================
    // REGISTER (backward-compatible overload for raw Customer)
    // =====================================================

    @Transactional
    public Customer registerCustomer(Customer customer) {

        if (customer == null) {
            throw new InvalidTransactionException("Registration details are required");
        }

        // Reject missing/invalid registration details before anything is
        // persisted. Without this, a null password would be BCrypt-encoded
        // to null (and violate the NOT NULL column) and a blank password
        // would be accepted. InvalidTransactionException -> 400 BAD_REQUEST.
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new InvalidTransactionException("Full name is required");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new InvalidTransactionException("Email is required");
        }
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new InvalidTransactionException("Phone number is required");
        }
        if (customer.getPassword() == null || customer.getPassword().isBlank()) {
            throw new InvalidTransactionException("Password is required");
        }
        if (customer.getPassword().length() < 6) {
            throw new InvalidTransactionException(
                    "Password must be at least 6 characters");
        }

        checkEmailAvailable(customer.getEmail());

        customer.setEmail(normaliseEmail(customer.getEmail()));
        customer.setPassword(
                passwordEncoder.encode(customer.getPassword())
        );
        customer.setRole("CUSTOMER");
        customer.setStatus("ACTIVE");

        Customer saved = customerRepository.save(customer);
        accountService.createAccount(saved, "SAVINGS");

        auditService.log("REGISTER", "New customer registered", saved.getEmail());

        return saved;
    }

    // =====================================================
    // READ
    // =====================================================

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: " + email));
    }

    public Customer getCurrentCustomer() {
        return currentUserService.getCurrentCustomer();
    }

    // =====================================================
    // UPDATE CURRENT PROFILE
    // Only the authenticated customer can change their own
    // name, phone and (optionally) email. Role/status/id can
    // never be changed here.
    // =====================================================

    @Transactional
    public Customer updateCurrentProfile(UpdateProfileRequest request) {

        Customer customer = currentUserService.getCurrentCustomer();

        return updateProfile(customer.getId(), request);
    }

    // =====================================================
    // UPDATE PROFILE BY CUSTOMER ID
    // Used for the current user (/me) and, after the ownership
    // check in the controller, for admin-managed updates.
    // Only name, phone and (optionally) email can change.
    // =====================================================

    @Transactional
    public Customer updateProfile(
            Long customerId, UpdateProfileRequest request) {

        if (request == null) {
            throw new InvalidTransactionException(
                    "Profile details are required");
        }

        Customer customer = getCustomerById(customerId);

        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone().trim());

        // Email is optional; when provided it must be unique
        // (excluding this customer's own current email).
        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            String newEmail = normaliseEmail(request.getEmail());

            if (!newEmail.equals(customer.getEmail())
                    && customerRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException(
                        "Email is already registered");
            }

            customer.setEmail(newEmail);
        }

        Customer saved = customerRepository.save(customer);

        auditService.log("PROFILE_UPDATE", "Profile updated", saved.getEmail());

        return saved;
    }

    // =====================================================
    // CHANGE OWN PASSWORD
    // Verifies the current password, then re-hashes the new
    // one with the existing BCrypt encoder. Never stores or
    // returns plaintext and never affects other users.
    // =====================================================

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        if (request == null) {
            throw new InvalidTransactionException(
                    "Password details are required");
        }

        Customer customer = currentUserService.getCurrentCustomer();

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                customer.getPassword())) {

            throw new InvalidTransactionException(
                    "Current password is incorrect");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 6) {
            throw new InvalidTransactionException(
                    "New password must be at least 6 characters");
        }

        if (!request.getNewPassword().equals(
                request.getConfirmPassword())) {
            throw new InvalidTransactionException(
                    "New password and confirmation do not match");
        }

        customer.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        customerRepository.save(customer);

        auditService.log(
                "PASSWORD_CHANGE", "Password changed", customer.getEmail());
    }

    // =====================================================
    // SAFE DEACTIVATION (keeps banking history intact)
    // =====================================================

    @Transactional
    public void deactivateCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setStatus("SUSPENDED");
        customerRepository.save(customer);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void checkEmailAvailable(String email) {
        if (customerRepository.existsByEmail(normaliseEmail(email))) {
            throw new DuplicateResourceException(
                    "Email is already registered");
        }
    }

    private String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}