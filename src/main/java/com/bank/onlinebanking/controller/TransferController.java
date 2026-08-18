package com.bank.onlinebanking.controller;

import com.bank.onlinebanking.dto.TransferRequest;
import com.bank.onlinebanking.dto.TransferResponse;
import com.bank.onlinebanking.service.CustomerService;
import com.bank.onlinebanking.service.TransferService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final CustomerService customerService;

    public TransferController(
            TransferService transferService,
            CustomerService customerService) {

        this.transferService = transferService;
        this.customerService = customerService;
    }

    // =====================================================
    // FUND TRANSFER
    // POST /api/transfers
    // =====================================================

    @PostMapping
    public ResponseEntity<TransferResponse> transferMoney(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response =
                transferService.transfer(
                        customerService.getCurrentCustomer().getId(),
                        request.getSenderAccountNumber(),
                        request.getReceiverAccountNumber(),
                        request.getAmount(),
                        request.getDescription()
                );

        return ResponseEntity.ok(response);
    }
}