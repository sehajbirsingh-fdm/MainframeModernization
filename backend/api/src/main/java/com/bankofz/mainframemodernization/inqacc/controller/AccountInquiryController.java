package com.bankofz.mainframemodernization.inqacc.controller;

import com.bankofz.mainframemodernization.inqacc.domain.AccountResponse;
import com.bankofz.mainframemodernization.inqacc.service.AccountInquiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
public class AccountInquiryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountInquiryController.class);

    private final AccountInquiryService accountInquiryService;

    public AccountInquiryController(AccountInquiryService accountInquiryService) {
        this.accountInquiryService = accountInquiryService;
    }

    @GetMapping("/{sortcode}/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String sortcode,
            @PathVariable String accountNumber,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        long startNanos = System.nanoTime();
        AccountResponse response = accountInquiryService.inquireAccount(sortcode, accountNumber);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        LOGGER.info("event=inqacc_inquiry_success path=/v1/accounts/{sortcode}/{accountNumber} status=200 durationMs={}", elapsedMs);
        return ResponseEntity.ok(response);
    }
}
