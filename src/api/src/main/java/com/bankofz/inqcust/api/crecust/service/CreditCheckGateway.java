package com.bankofz.inqcust.api.crecust.service;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerRequest;

import java.time.LocalDate;

public interface CreditCheckGateway {

    CreditCheckResult assess(CreateCustomerRequest request, LocalDate today);

    record CreditCheckResult(boolean success, int score, LocalDate reviewDate, String failCode) {
    }
}
