package com.bank.onlinebanking.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.exception.ResourceNotFoundException;
import com.bank.onlinebanking.repository.CustomerRepository;

/**
 * Resolves the currently authenticated customer from the security context.
 */
@Component
public class CurrentUserService {

    private final CustomerRepository customerRepository;

    public CurrentUserService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * @return email of the authenticated principal
     * @throws IllegalStateException when no user is authenticated
     */
    public String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        return auth.getName();
    }

    /**
     * @return the full Customer entity of the authenticated user
     */
    public Customer getCurrentCustomer() {
        return customerRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user no longer exists"));
    }
}