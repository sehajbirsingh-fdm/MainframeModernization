package com.bankofz.mainframemodernization.inqtran.controller;

import com.bankofz.mainframemodernization.inqtran.domain.CreateTransactionRequest;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.service.TransactionCreationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class TransactionCreationController {

    private final TransactionCreationService transactionCreationService;

    public TransactionCreationController(TransactionCreationService transactionCreationService) {
        this.transactionCreationService = transactionCreationService;
    }

    @PostMapping("/{sortCode}/{accountNumber}/transactions")
    public ResponseEntity<TransactionRecord> createTransaction(
            @PathVariable
            @Pattern(regexp = "^[0-9]{6}$", message = "sortCode must match ^[0-9]{6}$")
            String sortCode,
            @PathVariable
            @Pattern(regexp = "^[0-9]{8}$", message = "accountNumber must match ^[0-9]{8}$")
            String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionRecord created = transactionCreationService.create(sortCode, accountNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
