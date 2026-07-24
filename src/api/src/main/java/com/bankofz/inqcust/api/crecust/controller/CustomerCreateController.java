package com.bankofz.inqcust.api.crecust.controller;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerRequest;
import com.bankofz.inqcust.api.crecust.domain.CreateCustomerResponse;
import com.bankofz.inqcust.api.crecust.service.CustomerCreateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/customers")
public class CustomerCreateController {

    private final CustomerCreateService customerCreateService;

    public CustomerCreateController(CustomerCreateService customerCreateService) {
        this.customerCreateService = customerCreateService;
    }

    @PostMapping
    public ResponseEntity<CreateCustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        CreateCustomerResponse response = customerCreateService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
