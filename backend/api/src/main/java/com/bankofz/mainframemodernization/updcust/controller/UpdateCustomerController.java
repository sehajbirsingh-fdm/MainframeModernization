package com.bankofz.mainframemodernization.updcust.controller;

import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerResponse;
import com.bankofz.mainframemodernization.updcust.service.UpdateCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/customers")
public class UpdateCustomerController {

    private final UpdateCustomerService updateCustomerService;

    public UpdateCustomerController(UpdateCustomerService updateCustomerService) {
        this.updateCustomerService = updateCustomerService;
    }

    @PutMapping("/{customerNumber}")
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(
            @PathVariable
            @Pattern(regexp = "^[0-9]{1,10}$", message = "customerNumber must match ^[0-9]{1,10}$")
            String customerNumber,
            @RequestParam(required = false) String sortCode,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        UpdateCustomerResponse response = updateCustomerService.updateCustomer(sortCode, customerNumber, request);
        return ResponseEntity.ok(response);
    }
}
