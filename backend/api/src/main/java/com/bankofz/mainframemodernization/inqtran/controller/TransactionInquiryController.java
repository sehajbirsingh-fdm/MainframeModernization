package com.bankofz.mainframemodernization.inqtran.controller;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.service.TransactionInquiryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class TransactionInquiryController {

    private final TransactionInquiryService transactionInquiryService;

    public TransactionInquiryController(TransactionInquiryService transactionInquiryService) {
        this.transactionInquiryService = transactionInquiryService;
    }

    @GetMapping("/{sortCode}/{accountNumber}/transactions")
    public ResponseEntity<TransactionInquiryResponse> getTransactions(
            @PathVariable
            @Pattern(regexp = "^[0-9]{6}$", message = "sortCode must match ^[0-9]{6}$")
            String sortCode,
            @PathVariable
            @Pattern(regexp = "^[0-9]{8}$", message = "accountNumber must match ^[0-9]{8}$")
            String accountNumber,
            @RequestParam(required = false)
            @Pattern(regexp = "^[0-9]{8}$", message = "fromDate must match ^[0-9]{8}$")
            String fromDate,
            @RequestParam(required = false)
            @Pattern(regexp = "^[0-9]{8}$", message = "toDate must match ^[0-9]{8}$")
            String toDate,
            @RequestParam(required = false)
            @Min(value = 0, message = "limit must be >= 0")
            Integer limit,
            @RequestParam(required = false)
            @Min(value = 0, message = "offset must be >= 0")
            Integer offset
    ) {
        return ResponseEntity.ok(
                transactionInquiryService.inquire(sortCode, accountNumber, fromDate, toDate, limit, offset)
        );
    }
}
