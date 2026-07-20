package com.bankofz.inqcust.api.inqacc.mapper;

import com.bankofz.inqcust.api.inqacc.domain.AccountRecord;
import com.bankofz.inqcust.api.inqacc.domain.AccountResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AccountResponseMapper {

    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    public AccountResponse map(AccountRecord record) {
        return new AccountResponse(
                trim(record.eyecatcher()),
                trim(record.customerNumber()),
                trim(record.sortcode()),
                trim(record.accountNumber()),
                trim(record.accountType()),
                record.interestRate(),
                toIsoDate(record.accountOpened()),
                record.overdraftLimit(),
                toIsoDate(record.lastStatementDate()),
                toIsoDate(record.nextStatementDate()),
                record.availableBalance(),
                record.actualBalance()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String toIsoDate(Integer legacyDate) {
        if (legacyDate == null) {
            return null;
        }

        String formatted = String.format("%08d", legacyDate);
        LocalDate parsed = LocalDate.parse(formatted, LEGACY_DATE_FORMAT);
        return parsed.toString();
    }
}
