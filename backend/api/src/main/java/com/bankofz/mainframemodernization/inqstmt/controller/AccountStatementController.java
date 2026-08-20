package com.bankofz.mainframemodernization.inqstmt.controller;

import com.bankofz.mainframemodernization.inqstmt.domain.AccountStatementResponse;
import com.bankofz.mainframemodernization.inqstmt.service.AccountStatementService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    public AccountStatementController(AccountStatementService accountStatementService) {
        this.accountStatementService = accountStatementService;
    }

    @GetMapping("/{sortCode}/{accountNumber}/statements/{period}")
    public ResponseEntity<AccountStatementResponse> getStatement(
            @PathVariable
            @Pattern(regexp = "^[0-9]{6}$", message = "sortCode must match ^[0-9]{6}$")
            String sortCode,
            @PathVariable
            @Pattern(regexp = "^[0-9]{8}$", message = "accountNumber must match ^[0-9]{8}$")
            String accountNumber,
            @PathVariable
            @Pattern(regexp = "^[0-9]{4}(0[1-9]|1[0-2])$", message = "period must match ^[0-9]{4}(0[1-9]|1[0-2])$")
            String period
    ) {
        return ResponseEntity.ok(accountStatementService.retrieveStatement(sortCode, accountNumber, period));
    }
}
