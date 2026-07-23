package com.bankofz.inqcust.api.inqacccu.service;

import com.bankofz.inqcust.api.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.inqcust.api.inqacccu.domain.AccountSummary;
import com.bankofz.inqcust.api.inqacccu.domain.AccountsList;
import com.bankofz.inqcust.api.inqacccu.domain.CustomerSummary;
import com.bankofz.inqcust.api.inqacccu.domain.LegacyStatus;
import com.bankofz.inqcust.api.inqacccu.repository.model.AccountProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.CustomerProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountRelationshipMapper {

    private final DateMapper dateMapper;

    public AccountRelationshipMapper(DateMapper dateMapper) {
        this.dateMapper = dateMapper;
    }

    public AccountRelationshipResponse toSuccessResponse(RelationshipProjection projection) {
        CustomerSummary customer = toCustomerSummary(projection.customer());
        List<AccountSummary> accountSummaries = projection.accounts().stream()
                .map(this::toAccountSummary)
                .toList();

        return new AccountRelationshipResponse(
                new LegacyStatus("Y", "0000", "Y"),
                customer,
                new AccountsList(accountSummaries.size(), accountSummaries)
        );
    }

    public AccountRelationshipResponse toNotFoundResponse() {
        return new AccountRelationshipResponse(
                new LegacyStatus("N", "1001", "N"),
                null,
                null
        );
    }

    private CustomerSummary toCustomerSummary(CustomerProjection customer) {
        return new CustomerSummary(
                trim(customer.customerNumber()),
                trim(customer.customerName()),
                trim(customer.sortCode()),
                trim(customer.customerType())
        );
    }

    private AccountSummary toAccountSummary(AccountProjection account) {
        return new AccountSummary(
                trim(account.accountNumber()),
                trim(account.sortCode()),
                trim(account.accountType()),
                trim(account.accountTypeDescription()),
                account.availableBalance(),
                "GBP",
                account.actualBalance(),
                "GBP",
                account.interestRate(),
                account.overdraftLimit(),
                dateMapper.toIsoDate(account.lastStatementDate()),
                dateMapper.toIsoDate(account.nextStatementDate())
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
