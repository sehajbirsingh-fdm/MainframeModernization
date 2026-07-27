package com.bankofz.mainframemodernization.inqacccu.service;

import com.bankofz.mainframemodernization.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.mainframemodernization.inqacccu.domain.AccountSummary;
import com.bankofz.mainframemodernization.inqacccu.domain.LegacyStatus;
import com.bankofz.mainframemodernization.inqacccu.repository.model.AccountProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.CustomerProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.RelationshipProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountRelationshipMapper {

    private static final int MAX_ACCOUNTS = 20;
    private static final String EYECATCHER = "ACCT";
    private static final String FIXED_SORT_CODE = "987654";

    private final DateMapper dateMapper;

    public AccountRelationshipMapper(DateMapper dateMapper) {
        this.dateMapper = dateMapper;
    }

    public AccountRelationshipResponse toSuccessResponse(RelationshipProjection projection) {
        String customerNumber = trim(projection.customer().customerNumber());
        List<AccountSummary> accountSummaries = projection.accounts().stream()
            .limit(MAX_ACCOUNTS)
            .map(account -> toAccountSummary(account, customerNumber))
                .toList();

        return new AccountRelationshipResponse(
            new LegacyStatus("Y", "0", "Y"),
            customerNumber,
            accountSummaries.size(),
            accountSummaries
        );
    }

        public AccountRelationshipResponse toNotFoundResponse(String customerNumber) {
        return new AccountRelationshipResponse(
            new LegacyStatus("N", "1", "N"),
            trim(customerNumber),
            0,
            List.of()
        );
    }

        public AccountRelationshipResponse toRetrievalFailureResponse(String customerNumber, String failCode) {
            return new AccountRelationshipResponse(
                new LegacyStatus("N", failCode, "Y"),
                trim(customerNumber),
                0,
                List.of()
            );
        }

        private AccountSummary toAccountSummary(AccountProjection account, String customerNumber) {
        return new AccountSummary(
            EYECATCHER,
            customerNumber,
            FIXED_SORT_CODE,
                trim(account.accountNumber()),
                trim(account.accountType()),
                account.interestRate(),
            dateMapper.toIsoDate(account.openedDate()),
                account.overdraftLimit(),
                dateMapper.toIsoDate(account.lastStatementDate()),
            dateMapper.toIsoDate(account.nextStatementDate()),
            account.availableBalance(),
            account.actualBalance()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
