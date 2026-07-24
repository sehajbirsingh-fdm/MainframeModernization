package com.bankofz.inqcust.api.crecust.service;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MockCreditCheckGateway implements CreditCheckGateway {

    @Override
    public CreditCheckResult assess(CreateCustomerRequest request, LocalDate today) {
        String firstName = request.firstName() == null ? "" : request.firstName().trim().toUpperCase();

        // Allows deterministic failure-path testing without external integrations.
        if (firstName.startsWith("NOCREDIT") || firstName.startsWith("FAIL_G")) {
            return new CreditCheckResult(false, 0, today, "G");
        }

        int hash = Math.abs((request.firstName() + "|" + request.lastName() + "|" + request.phone()).hashCode());
        int score = 300 + (hash % 551);
        LocalDate reviewDate = today.plusDays((hash % 21) + 1L);
        return new CreditCheckResult(true, score, reviewDate, " ");
    }
}
