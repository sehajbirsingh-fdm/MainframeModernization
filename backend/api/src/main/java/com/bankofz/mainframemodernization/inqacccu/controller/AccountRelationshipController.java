package com.bankofz.mainframemodernization.inqacccu.controller;

import com.bankofz.mainframemodernization.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.mainframemodernization.inqacccu.service.AccountRelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/customers")
public class AccountRelationshipController {

    private final AccountRelationshipService service;

    public AccountRelationshipController(AccountRelationshipService service) {
        this.service = service;
    }

    @Operation(summary = "Retrieve customer-account relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inquiry completed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Internal processing error")
    })
    @GetMapping("/{customerNumber}/accounts")
    public AccountRelationshipResponse inquire(
            @PathVariable
            @Pattern(regexp = "^[0-9]{10}$", message = "customerNumber must match ^[0-9]{10}$")
            String customerNumber
    ) {
        return service.inquire(customerNumber);
    }

    @GetMapping("/accounts")
    public AccountRelationshipResponse inquireWithMissingCustomerNumber() {
        throw new IllegalArgumentException("customerNumber is required path parameter");
    }
}
