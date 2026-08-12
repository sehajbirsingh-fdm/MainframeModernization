package com.bankofz.mainframemodernization.crecust.service;

import com.bankofz.mainframemodernization.crecust.domain.CreateCustomerRequest;
import com.bankofz.mainframemodernization.crecust.domain.CreateCustomerResponse;
import com.bankofz.mainframemodernization.crecust.domain.DateParts;
import com.bankofz.mainframemodernization.crecust.domain.LegacyCreateStatus;
import com.bankofz.mainframemodernization.crecust.repository.CustomerCreateRepository;
import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import com.bankofz.mainframemodernization.inqcust.service.LegacyDateConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CustomerCreateService {

    private static final Set<String> ALLOWED_TITLES = Set.of(
            "Professor", "Mr", "Mrs", "Miss", "Ms", "Dr", "Drs", "Lord", "Sir", "Lady", ""
    );

    private final CustomerCreateRepository repository;
    private final CreditCheckGateway creditCheckGateway;
    private final LegacyDateConverter legacyDateConverter;
    private final String sortCode;
    private final ConcurrentMap<String, ReentrantLock> sortCodeLocks = new ConcurrentHashMap<>();

    public CustomerCreateService(
            CustomerCreateRepository repository,
            CreditCheckGateway creditCheckGateway,
            LegacyDateConverter legacyDateConverter,
            @Value("${app.crecust.sortcode:123456}") String sortCode
    ) {
        this.repository = repository;
        this.creditCheckGateway = creditCheckGateway;
        this.legacyDateConverter = legacyDateConverter;
        this.sortCode = sortCode;
    }

    public CreateCustomerResponse createCustomer(CreateCustomerRequest request) {
        validateTitle(request.title());

        LocalDate today = LocalDate.now();
        CreditCheckGateway.CreditCheckResult creditResult = creditCheckGateway.assess(request, today);
        if (!creditResult.success()) {
            throw new CustomerCreateException(
                    "Credit check did not return usable data",
                    "ERR-301",
                    creditResult.failCode(),
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        LocalDate dob = validateDob(request.dateOfBirth(), today);
        LocalDate createdDate = validateDateParts(request.createdDate(), "createdDate");

        ReentrantLock lock = sortCodeLocks.computeIfAbsent(sortCode, ignored -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new CustomerCreateException(
                    "Unable to acquire customer number lock",
                    "ERR-202",
                    "3",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        CreateCustomerResponse response = null;
        RuntimeException operationFailure = null;
        CustomerCreateException lockStateAnomaly = null;

        try {
            long nextNumber;
            try {
                nextNumber = repository.nextCustomerNumber(sortCode);
            } catch (Exception exception) {
                throw new CustomerCreateException(
                        "Unable to allocate next customer number",
                        "ERR-203",
                        "4",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            String customerNumber = String.format("%010d", nextNumber);
            CustomerRecord customerRecord = new CustomerRecord(
                    "CUST",
                    sortCode,
                    customerNumber,
                    sanitize(request.title(), 10),
                    sanitize(request.firstName(), 50),
                    sanitize(request.lastName(), 50),
                    toLegacyInt(dob),
                    sanitize(request.phone(), 20),
                    sanitize(request.address().line1(), 50),
                    sanitize(request.address().line2(), 50),
                    sanitize(request.address().city(), 50),
                    sanitize(request.address().postcode(), 10),
                    sanitize(request.address().country(), 50),
                    sanitize(request.status(), 10),
                    toLegacyInt(createdDate),
                    creditResult.score(),
                    toLegacyInt(creditResult.reviewDate())
            );

            CustomerRecord saved;
            try {
                saved = repository.save(customerRecord);
            } catch (Exception exception) {
                throw new CustomerCreateException(
                        "Unable to persist customer",
                        "ERR-201",
                        "1",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

                response = new CreateCustomerResponse(
                    trim(saved.eyecatcher()),
                    trim(saved.sortCode()),
                    trim(saved.customerNumber()),
                    trim(saved.title()),
                    trim(saved.firstName()),
                    trim(saved.lastName()),
                    legacyDateConverter.toLocalDate(saved.dateOfBirth()),
                    trim(saved.phone()),
                    trim(saved.addressLine1()),
                    trim(saved.addressLine2()),
                    trim(saved.city()),
                    trim(saved.postcode()),
                    trim(saved.country()),
                    trim(saved.status()),
                    legacyDateConverter.toLocalDate(saved.createdDate()),
                    saved.creditScore(),
                    legacyDateConverter.toLocalDate(saved.creditScoreReviewDate()),
                    new LegacyCreateStatus("Y", " ")
            );
        } catch (RuntimeException exception) {
            operationFailure = exception;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            } else {
                lockStateAnomaly = new CustomerCreateException(
                        "Unable to release customer number lock",
                        "ERR-204",
                        "5",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }
        }

        if (operationFailure != null) {
            if (lockStateAnomaly != null) {
                operationFailure.addSuppressed(lockStateAnomaly);
            }
            throw operationFailure;
        }

        if (lockStateAnomaly != null) {
            throw lockStateAnomaly;
        }

        return response;
    }

    private void validateTitle(String title) {
        String normalized = title == null ? "" : title.stripTrailing();
        if (!ALLOWED_TITLES.contains(normalized)) {
            throw new CustomerCreateException(
                    "Title is invalid. Allowed values are Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, or blank.",
                    "ERR-101",
                    "T",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    private LocalDate validateDob(DateParts dob, LocalDate today) {
        if (dob.year() < 1601) {
            throw new CustomerCreateException("Date of birth year is out of range", "ERR-102", "0", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        LocalDate value;
        try {
            value = LocalDate.of(dob.year(), dob.month(), dob.day());
        } catch (DateTimeException exception) {
            throw new CustomerCreateException("Date of birth is invalid", "ERR-103", "Z", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (value.isAfter(today)) {
            throw new CustomerCreateException("Date of birth cannot be in the future", "ERR-104", "Y", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (Period.between(value, today).getYears() > 150) {
            throw new CustomerCreateException("Date of birth implies age greater than 150", "ERR-102", "0", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return value;
    }

    private LocalDate validateDateParts(DateParts dateParts, String fieldName) {
        try {
            return LocalDate.of(dateParts.year(), dateParts.month(), dateParts.day());
        } catch (DateTimeException exception) {
            throw new CustomerCreateException(
                    fieldName + " is invalid",
                    "ERR-001",
                    " ",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private int toLegacyInt(LocalDate localDate) {
        return Integer.parseInt(localDate.toString().replace("-", ""));
    }

    private String sanitize(String value, int maxLen) {
        String text = value == null ? "" : value;
        if (text.length() > maxLen) {
            return text.substring(0, maxLen);
        }
        return text;
    }

    private String trim(String value) {
        return value == null ? null : value.stripTrailing();
    }

    public String correlationId(String providedCorrelationId) {
        if (providedCorrelationId != null && !providedCorrelationId.isBlank()) {
            return providedCorrelationId;
        }
        return UUID.randomUUID().toString();
    }
}
