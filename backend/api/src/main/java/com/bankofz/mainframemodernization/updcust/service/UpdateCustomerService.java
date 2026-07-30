package com.bankofz.mainframemodernization.updcust.service;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import com.bankofz.mainframemodernization.inqcust.repository.CustomerRepository;
import com.bankofz.mainframemodernization.inqcust.service.LegacyDateConverter;
import com.bankofz.mainframemodernization.updcust.domain.LegacyUpdateStatus;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerAddressRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerResponse;
import com.bankofz.mainframemodernization.updcust.repository.UpdateCustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
public class UpdateCustomerService {

    private static final Set<String> ALLOWED_TITLES = Set.of(
            "Professor", "Mr", "Mrs", "Miss", "Ms", "Dr", "Drs", "Lord", "Sir", "Lady", ""
    );

    private final CustomerRepository customerRepository;
    private final UpdateCustomerRepository updateCustomerRepository;
    private final LegacyDateConverter legacyDateConverter;
    private final String defaultSortCode;

    public UpdateCustomerService(
            CustomerRepository customerRepository,
            UpdateCustomerRepository updateCustomerRepository,
            LegacyDateConverter legacyDateConverter,
            @Value("${app.updcust.sortcode:987654}") String defaultSortCode
    ) {
        this.customerRepository = customerRepository;
        this.updateCustomerRepository = updateCustomerRepository;
        this.legacyDateConverter = legacyDateConverter;
        this.defaultSortCode = defaultSortCode;
    }

    public UpdateCustomerResponse updateCustomer(String sortCodeInput, String customerNumberInput, UpdateCustomerRequest request) {
        validateSortCode(sortCodeInput);
        String resolvedSortCode = resolveSortCode(sortCodeInput);
        String normalizedCustomerNumber = normalizeCustomerNumber(customerNumberInput);

        validateTitle(request.title());
        validateMeaningfulUpdate(request);

        CustomerRecord existing;
        try {
            Optional<CustomerRecord> maybeExisting = customerRepository
                    .findBySortCodeAndCustomerNumber(resolvedSortCode, normalizedCustomerNumber);
            if (maybeExisting.isEmpty()) {
                throw new UpdateCustomerException(
                        "Customer not found",
                        "UPDCUST-404",
                        "1",
                        HttpStatus.NOT_FOUND
                );
            }
            existing = maybeExisting.get();
        } catch (UpdateCustomerException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UpdateCustomerException(
                    "Failed to read customer record",
                    "UPDCUST-500-READ",
                    "2",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        CustomerRecord updatedRecord = applyLegacyUpdateRules(existing, request);

        CustomerRecord saved;
        try {
            saved = updateCustomerRepository.update(updatedRecord);
        } catch (Exception exception) {
            throw new UpdateCustomerException(
                    "Failed to update customer record",
                    "UPDCUST-500-UPDATE",
                    "3",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return toResponse(saved);
    }

    private CustomerRecord applyLegacyUpdateRules(CustomerRecord existing, UpdateCustomerRequest request) {
        String nextTitle = existing.title();
        String nextFirstName = existing.firstName();
        String nextLastName = existing.lastName();
        Integer nextDob = existing.dateOfBirth();
        String nextPhone = existing.phone();
        String nextAddressLine1 = existing.addressLine1();
        String nextAddressLine2 = existing.addressLine2();
        String nextCity = existing.city();
        String nextPostcode = existing.postcode();
        String nextCountry = existing.country();
        String nextStatus = existing.status();

        if (firstCharacterNotSpace(request.firstName())) {
            nextTitle = sanitize(request.title(), 10);
            nextFirstName = sanitize(request.firstName(), 50);
            nextLastName = sanitize(request.lastName(), 50);
        }

        if (firstCharacterNotSpace(request.phoneNumber())) {
            nextPhone = sanitize(request.phoneNumber(), 20);
        }

        UpdateCustomerAddressRequest address = request.address();
        String addressLine1 = address == null ? null : address.addressLine1();
        if (firstCharacterNotSpace(addressLine1)) {
            nextAddressLine1 = sanitize(address.addressLine1(), 50);
            nextAddressLine2 = sanitize(address.addressLine2(), 50);
            nextCity = sanitize(address.city(), 50);
            nextPostcode = sanitize(address.postalCode(), 10);
            nextCountry = sanitize(address.country(), 50);
        }

        if (firstCharacterNotSpace(request.customerStatus())) {
            nextStatus = sanitize(request.customerStatus(), 10);
        }

        if (request.dateOfBirth() != null && !request.dateOfBirth().isBlank()) {
            LocalDate dob;
            try {
                dob = LocalDate.parse(request.dateOfBirth());
            } catch (Exception exception) {
                throw new UpdateCustomerException(
                        "dateOfBirth must match yyyy-MM-dd",
                        "UPDCUST-400-DOB",
                        " ",
                        HttpStatus.BAD_REQUEST
                );
            }
            nextDob = toLegacyInt(dob);
        }

        return new CustomerRecord(
                existing.eyecatcher(),
                existing.sortCode(),
                existing.customerNumber(),
                nextTitle,
                nextFirstName,
                nextLastName,
                nextDob,
                nextPhone,
                nextAddressLine1,
                nextAddressLine2,
                nextCity,
                nextPostcode,
                nextCountry,
                nextStatus,
                existing.createdDate(),
                existing.creditScore(),
                existing.creditScoreReviewDate()
        );
    }

    private UpdateCustomerResponse toResponse(CustomerRecord customerRecord) {
        return new UpdateCustomerResponse(
                trim(customerRecord.customerNumber()),
                trim(customerRecord.sortCode()),
                trim(customerRecord.title()),
                trim(customerRecord.firstName()),
                trim(customerRecord.lastName()),
                legacyDateConverter.toLocalDate(customerRecord.dateOfBirth()),
                trim(customerRecord.phone()),
                new UpdateCustomerAddressRequest(
                        trim(customerRecord.addressLine1()),
                        trim(customerRecord.addressLine2()),
                        trim(customerRecord.city()),
                        trim(customerRecord.postcode()),
                        trim(customerRecord.country())
                ),
                trim(customerRecord.status()),
                legacyDateConverter.toLocalDate(customerRecord.createdDate()),
                customerRecord.creditScore(),
                legacyDateConverter.toLocalDate(customerRecord.creditScoreReviewDate()),
                new LegacyUpdateStatus("Y", " ")
        );
    }

    private void validateTitle(String title) {
        String normalized = title == null ? "" : title.stripTrailing();
        if (!ALLOWED_TITLES.contains(normalized)) {
            throw new UpdateCustomerException(
                    "Title is invalid. Allowed values are Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, or blank.",
                    "UPDCUST-422-TITLE",
                    "T",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    private void validateMeaningfulUpdate(UpdateCustomerRequest request) {
        String addressLine1 = request.address() == null ? null : request.address().addressLine1();
        if (isLegacyBlank(request.firstName()) && isLegacyBlank(request.lastName()) && isLegacyBlank(addressLine1)) {
            throw new UpdateCustomerException(
                    "At least one meaningful update is required in firstName, lastName, or addressLine1.",
                    "UPDCUST-422-PAYLOAD",
                    "4",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    private String normalizeCustomerNumber(String customerNumberInput) {
        String normalizedInput = customerNumberInput == null ? "" : customerNumberInput.trim();
        try {
            long value = Long.parseLong(normalizedInput);
            if (value < 0 || value > 9_999_999_999L) {
                throw new NumberFormatException("out of range");
            }
            return String.format("%010d", value);
        } catch (NumberFormatException exception) {
            throw new UpdateCustomerException(
                    "customerNumber must be numeric and no more than 10 digits",
                    "UPDCUST-400-CUSTNO",
                    " ",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String resolveSortCode(String sortCodeInput) {
        if (isBlankOrLowValue(sortCodeInput)) {
            return defaultSortCode;
        }
        return sortCodeInput;
    }

    private void validateSortCode(String sortCodeInput) {
        if (sortCodeInput == null || sortCodeInput.isBlank()) {
            return;
        }
        if (!sortCodeInput.matches("^[0-9]{6}$")) {
            throw new UpdateCustomerException(
                    "sortCode must match ^[0-9]{6}$",
                    "UPDCUST-400-SORTCODE",
                    " ",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private boolean isBlankOrLowValue(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > ' ') {
                return false;
            }
        }
        return true;
    }

    private boolean firstCharacterNotSpace(String value) {
        return value != null && !value.isEmpty() && value.charAt(0) != ' ';
    }

    private boolean isLegacyBlank(String value) {
        return value == null || value.isBlank() || value.charAt(0) == ' ';
    }

    private int toLegacyInt(LocalDate localDate) {
        return Integer.parseInt(localDate.toString().replace("-", ""));
    }

    private String sanitize(String value, int maxLength) {
        String text = value == null ? "" : value;
        if (text.length() > maxLength) {
            return text.substring(0, maxLength);
        }
        return text;
    }

    private String trim(String value) {
        return value == null ? null : value.stripTrailing();
    }
}
