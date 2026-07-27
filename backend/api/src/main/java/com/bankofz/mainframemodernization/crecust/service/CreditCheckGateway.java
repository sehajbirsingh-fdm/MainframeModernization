package com.bankofz.mainframemodernization.crecust.service;

import com.bankofz.mainframemodernization.crecust.domain.CreateCustomerRequest;

import java.time.LocalDate;

public interface CreditCheckGateway {

    CreditCheckResult assess(CreateCustomerRequest request, LocalDate today);

    record CreditCheckResult(boolean success, int score, LocalDate reviewDate, String failCode) {
    }
}
