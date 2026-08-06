package com.bankofz.mainframemodernization.updcust.service;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import com.bankofz.mainframemodernization.inqcust.repository.CustomerRepository;
import com.bankofz.mainframemodernization.inqcust.service.LegacyDateConverter;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerAddressRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerResponse;
import com.bankofz.mainframemodernization.updcust.repository.UpdateCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateCustomerServiceTest {

    private CustomerRepository customerRepository;
    private UpdateCustomerRepository updateCustomerRepository;
    private UpdateCustomerService service;

    @BeforeEach
    void setUp() {
        customerRepository = org.mockito.Mockito.mock(CustomerRepository.class);
        updateCustomerRepository = org.mockito.Mockito.mock(UpdateCustomerRepository.class);
        service = new UpdateCustomerService(customerRepository, updateCustomerRepository, new LegacyDateConverter(), "987654");
    }

    @Test
    void updatesCustomerWhenValidRequest() {
        CustomerRecord existing = sampleRecord();
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                .thenReturn(Optional.of(existing));
        when(updateCustomerRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResponse response = service.updateCustomer("123456", "1", validRequest());

        assertEquals("Y", response.legacyStatus().updSuccess());
        assertEquals(" ", response.legacyStatus().updFailCode());
        assertEquals("Jane", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("0000000001", response.customerNumber());
    }

    @Test
    void rejectsInvalidTitleWithLegacyFailCodeT() {
        UpdateCustomerException exception = assertThrows(
                UpdateCustomerException.class,
                () -> service.updateCustomer("123456", "1", new UpdateCustomerRequest(
                        "Captain",
                        "Jane",
                        "Doe",
                        "1990-01-01",
                        "4165550111",
                        new UpdateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                        "ACTIVE"
                ))
        );

        assertEquals("T", exception.legacyFailCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void rejectsMeaninglessPayloadWithLegacyFailCode4() {
        UpdateCustomerException exception = assertThrows(
                UpdateCustomerException.class,
                () -> service.updateCustomer("123456", "1", new UpdateCustomerRequest(
                        "Mr",
                        "",
                        "",
                        "",
                        "",
                        new UpdateCustomerAddressRequest("", "", "", "", ""),
                        ""
                ))
        );

        assertEquals("4", exception.legacyFailCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void returnsNotFoundWithLegacyFailCode1() {
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                .thenReturn(Optional.empty());

        UpdateCustomerException exception = assertThrows(
                UpdateCustomerException.class,
                () -> service.updateCustomer("123456", "1", validRequest())
        );

        assertEquals("1", exception.legacyFailCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.httpStatus());
    }

    @Test
    void mapsReadFailureToLegacyFailCode2() {
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                .thenThrow(new IllegalStateException("read failure"));

        UpdateCustomerException exception = assertThrows(
                UpdateCustomerException.class,
                () -> service.updateCustomer("123456", "1", validRequest())
        );

        assertEquals("2", exception.legacyFailCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.httpStatus());
    }

    @Test
    void mapsUpdateFailureToLegacyFailCode3() {
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                .thenReturn(Optional.of(sampleRecord()));
        when(updateCustomerRepository.update(any()))
                .thenThrow(new IllegalStateException("update failure"));

        UpdateCustomerException exception = assertThrows(
                UpdateCustomerException.class,
                () -> service.updateCustomer("123456", "1", validRequest())
        );

        assertEquals("3", exception.legacyFailCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.httpStatus());
    }

    @Test
    void usesDefaultSortCodeWhenSortCodeMissing() {
        when(customerRepository.findBySortCodeAndCustomerNumber("987654", "0000000001"))
                .thenReturn(Optional.of(sampleRecord()));
        when(updateCustomerRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResponse response = service.updateCustomer("", "1", validRequest());

        assertEquals("Y", response.legacyStatus().updSuccess());
    }

    @Test
    void supportsNoOpParitySuccess() {
        CustomerRecord existing = sampleRecord();
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                .thenReturn(Optional.of(existing));
        when(updateCustomerRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResponse response = service.updateCustomer("123456", "1", new UpdateCustomerRequest(
                "Mr",
                " Jane",
                "Doe",
                "",
                "",
                new UpdateCustomerAddressRequest("", "", "", "", ""),
                ""
        ));

        assertEquals("John", response.firstName());
        assertEquals("Y", response.legacyStatus().updSuccess());
    }

        @Test
        void successAlwaysReturnsBlankFailCode() {
                CustomerRecord existing = sampleRecord();
                when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                                .thenReturn(Optional.of(existing));
                when(updateCustomerRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

                UpdateCustomerResponse response = service.updateCustomer("123456", "1", validRequest());

                assertEquals("Y", response.legacyStatus().updSuccess());
                assertEquals(" ", response.legacyStatus().updFailCode());
        }

        @Test
        void mapsCreditScoreReviewDateFromNumericYyyyMmDdToIsoDate() {
                CustomerRecord existing = sampleRecord();
                CustomerRecord withKnownReviewDate = new CustomerRecord(
                                existing.eyecatcher(),
                                existing.sortCode(),
                                existing.customerNumber(),
                                existing.title(),
                                existing.firstName(),
                                existing.lastName(),
                                existing.dateOfBirth(),
                                existing.phone(),
                                existing.addressLine1(),
                                existing.addressLine2(),
                                existing.city(),
                                existing.postcode(),
                                existing.country(),
                                existing.status(),
                                existing.createdDate(),
                                existing.creditScore(),
                                20261231
                );

                when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
                                .thenReturn(Optional.of(existing));
                when(updateCustomerRepository.update(any())).thenReturn(withKnownReviewDate);

                UpdateCustomerResponse response = service.updateCustomer("123456", "1", validRequest());

                assertEquals(LocalDate.of(2026, 12, 31), response.creditScoreReviewDate());
        }

    private UpdateCustomerRequest validRequest() {
        return new UpdateCustomerRequest(
                "Ms",
                "Jane",
                "Doe",
                "1990-01-01",
                "4165550111",
                new UpdateCustomerAddressRequest("10 Bay Street", "Suite 200", "Toronto", "M5J2N8", "Canada"),
                "SUSPENDED"
        );
    }

    private CustomerRecord sampleRecord() {
        return new CustomerRecord(
                "CUST",
                "123456",
                "0000000001",
                "Mr",
                "John",
                "Smith",
                19750101,
                "4165550101",
                "1 Main Street",
                "Suite 100",
                "Toronto",
                "M5H2N2",
                "Canada",
                "ACTIVE",
                20100615,
                742,
                20260115
        );
    }
}
