package com.bankofz.mainframemodernization.inqcust.mapper;

import com.bankofz.mainframemodernization.inqcust.domain.AddressResponse;
import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import com.bankofz.mainframemodernization.inqcust.domain.CustomerResponse;
import com.bankofz.mainframemodernization.inqcust.domain.CustomerStatus;
import com.bankofz.mainframemodernization.inqcust.service.LegacyDateConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    private final LegacyDateConverter legacyDateConverter;

    public CustomerMapper(LegacyDateConverter legacyDateConverter) {
        this.legacyDateConverter = legacyDateConverter;
    }

    public CustomerResponse map(CustomerRecord customerRecord) {
        return new CustomerResponse(
                trim(customerRecord.eyecatcher()),
                trim(customerRecord.sortCode()),
                trim(customerRecord.customerNumber()),
                trim(customerRecord.title()),
                trim(customerRecord.firstName()),
                trim(customerRecord.lastName()),
                legacyDateConverter.toLocalDate(customerRecord.dateOfBirth()),
                trim(customerRecord.phone()),
                new AddressResponse(
                        trim(customerRecord.addressLine1()),
                        trim(customerRecord.addressLine2()),
                        trim(customerRecord.city()),
                        trim(customerRecord.postcode()),
                        trim(customerRecord.country())
                ),
                CustomerStatus.valueOf(trim(customerRecord.status()).toUpperCase()),
                legacyDateConverter.toLocalDate(customerRecord.createdDate()),
                customerRecord.creditScore(),
                legacyDateConverter.toLocalDate(customerRecord.creditScoreReviewDate())
        );
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.stripTrailing();
    }
}
