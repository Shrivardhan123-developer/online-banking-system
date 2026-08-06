package com.bank.onlinebanking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bank.onlinebanking.dto.LoginRequest;
import com.bank.onlinebanking.dto.LoginResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.repository.CustomerRepository;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Customer customer = customerRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        String enteredPassword = request.getPassword();
        String storedPassword = customer.getPassword();

        boolean passwordMatches;

        /*
         * Existing legacy password migration.
         */
        if (isBCryptPassword(storedPassword)) {

            passwordMatches = passwordEncoder.matches(
                    enteredPassword,
                    storedPassword
            );

        } else {

            passwordMatches = enteredPassword.equals(storedPassword);

            if (passwordMatches) {

                customer.setPassword(
                        passwordEncoder.encode(enteredPassword)
                );

                customerRepository.save(customer);
            }
        }

        if (!passwordMatches) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                customer.getEmail()
        );

        return new LoginResponse(
                "Login successful",
                customer.getEmail(),
                token
        );
    }

    private boolean isBCryptPassword(String password) {

        return password != null &&
                (password.startsWith("$2a$")
                        || password.startsWith("$2b$")
                        || password.startsWith("$2y$"));
    }
}