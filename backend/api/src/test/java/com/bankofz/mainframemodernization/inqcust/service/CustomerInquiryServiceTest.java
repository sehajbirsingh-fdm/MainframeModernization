package com.bankofz.mainframemodernization.inqcust.service;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerInquiryResponse;
import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import com.bankofz.mainframemodernization.inqcust.domain.LookupMode;
import com.bankofz.mainframemodernization.inqcust.mapper.CustomerMapper;
import com.bankofz.mainframemodernization.inqcust.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerInquiryServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RandomCustomerNumberGenerator randomCustomerNumberGenerator;

    private CustomerInquiryService service;

    @BeforeEach
    void setUp() {
        LookupModeResolver lookupModeResolver = new LookupModeResolver();
        LegacyDateConverter legacyDateConverter = new LegacyDateConverter();
        CustomerMapper customerMapper = new CustomerMapper(legacyDateConverter);
        Clock clock = Clock.fixed(Instant.parse("2026-07-08T00:00:00Z"), ZoneOffset.UTC);
        RiskAssessmentService riskAssessmentService = new RiskAssessmentService(clock);
        LegacyStatusFactory legacyStatusFactory = new LegacyStatusFactory();

        service = new CustomerInquiryService(
                lookupModeResolver,
            customerRepository,
                customerMapper,
                riskAssessmentService,
                legacyStatusFactory,
            randomCustomerNumberGenerator,
                3
        );
    }

    @Test
    void specificCustomerFound() {
        when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000001"))
            .thenReturn(Optional.of(record("123456", "0000000001", "ACTIVE", 742, 20260115)));

        CustomerInquiryResponse response = service.inquire("123456", "0000000001");

        assertEquals(LookupMode.SPECIFIC, response.lookupMode());
        assertEquals("Y", response.legacyStatus().inquirySuccess());
        assertEquals("0", response.legacyStatus().inquiryFailCode());
        assertEquals("0000000001", response.customer().customerNumber());
        assertNotNull(response.riskAssessment());
    }

    @Test
    void specificCustomerNotFound() {
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000009999"))
        .thenReturn(Optional.empty());

        CustomerInquiryResponse response = service.inquire("123456", "0000009999");

        assertEquals(LookupMode.SPECIFIC, response.lookupMode());
        assertEquals("N", response.legacyStatus().inquirySuccess());
        assertEquals("1", response.legacyStatus().inquiryFailCode());
        assertNull(response.customer());
        assertNull(response.riskAssessment());
    }

    @Test
    void latestCustomerFound() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "0000000005", "INACTIVE", 720, 20260201)));

        CustomerInquiryResponse response = service.inquire("123456", "9999999999");

        assertEquals(LookupMode.LATEST, response.lookupMode());
        assertEquals("0000000005", response.customer().customerNumber());
        assertEquals("Y", response.legacyStatus().inquirySuccess());
    }

    @Test
    void latestCustomerNotFound() {
    when(customerRepository.findLatestBySortCode("999999"))
        .thenReturn(Optional.empty());

        CustomerInquiryResponse response = service.inquire("999999", "9999999999");

        assertEquals(LookupMode.LATEST, response.lookupMode());
        assertEquals("N", response.legacyStatus().inquirySuccess());
        assertEquals("9", response.legacyStatus().inquiryFailCode());
        assertNull(response.customer());
    }

    @Test
    void randomLookupGetsLatestThenFindsCustomer() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "0000000005", "INACTIVE", 720, 20260201)));
    when(randomCustomerNumberGenerator.nextCustomerNumber(5L)).thenReturn("0000000002");
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000002"))
        .thenReturn(Optional.of(record("123456", "0000000002", "ACTIVE", 650, 20250510)));

        CustomerInquiryResponse response = service.inquire("123456", "0000000000");

        assertEquals(LookupMode.RANDOM, response.lookupMode());
        assertEquals("Y", response.legacyStatus().inquirySuccess());
        assertEquals("0000000002", response.customer().customerNumber());
    verify(customerRepository).findLatestBySortCode("123456");
        verify(randomCustomerNumberGenerator).nextCustomerNumber(5L);
    verify(customerRepository).findBySortCodeAndCustomerNumber("123456", "0000000002");
    verify(customerRepository, never()).findBySortCodeAndCustomerNumber("123456", "0000000000");
    }

    @Test
    void randomLookupRetriesUntilFound() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "0000000005", "INACTIVE", 720, 20260201)));
    when(randomCustomerNumberGenerator.nextCustomerNumber(5L))
        .thenReturn("0000000004", "0000000002");
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000004"))
        .thenReturn(Optional.empty());
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000002"))
        .thenReturn(Optional.of(record("123456", "0000000002", "ACTIVE", 650, 20250510)));

        CustomerInquiryResponse response = service.inquire("123456", "0000000000");

        assertEquals(LookupMode.RANDOM, response.lookupMode());
    assertEquals("Y", response.legacyStatus().inquirySuccess());
    assertEquals("0000000002", response.customer().customerNumber());
    verify(randomCustomerNumberGenerator, times(2)).nextCustomerNumber(5L);
    verify(customerRepository).findBySortCodeAndCustomerNumber("123456", "0000000004");
    verify(customerRepository).findBySortCodeAndCustomerNumber("123456", "0000000002");
    }

    @Test
    void randomLookupReturnsNotFoundWhenLatestMissing() {
    when(customerRepository.findLatestBySortCode("123456")).thenReturn(Optional.empty());

        CustomerInquiryResponse response = service.inquire("123456", "0000000000");

    assertEquals(LookupMode.RANDOM, response.lookupMode());
    assertEquals("N", response.legacyStatus().inquirySuccess());
    assertEquals("1", response.legacyStatus().inquiryFailCode());
    assertNull(response.customer());
        verify(randomCustomerNumberGenerator, never()).nextCustomerNumber(anyLong());
    }

    @Test
    void randomLookupReturnsNotFoundAfterRetryLimit() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "0000000005", "INACTIVE", 720, 20260201)));
    when(randomCustomerNumberGenerator.nextCustomerNumber(5L))
        .thenReturn("0000000004", "0000000004", "0000000004", "0000000004");
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000004"))
        .thenReturn(Optional.empty());

    service = new CustomerInquiryService(
        new LookupModeResolver(),
        customerRepository,
        new CustomerMapper(new LegacyDateConverter()),
        new RiskAssessmentService(Clock.fixed(Instant.parse("2026-07-08T00:00:00Z"), ZoneOffset.UTC)),
        new LegacyStatusFactory(),
        randomCustomerNumberGenerator,
        4
    );

    CustomerInquiryResponse response = service.inquire("123456", "0000000000");

    assertEquals(LookupMode.RANDOM, response.lookupMode());
    assertEquals("N", response.legacyStatus().inquirySuccess());
    assertEquals("1", response.legacyStatus().inquiryFailCode());
    assertNull(response.customer());
    verify(randomCustomerNumberGenerator, times(4)).nextCustomerNumber(5L);
    verify(customerRepository, times(4)).findBySortCodeAndCustomerNumber("123456", "0000000004");
    }

    @Test
    void randomLookupSupportsTenDigitCustomerNumbersAboveIntegerMaxValue() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "3000000000", "INACTIVE", 720, 20260201)));
    when(randomCustomerNumberGenerator.nextCustomerNumber(3000000000L)).thenReturn("2147483648");
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "2147483648"))
        .thenReturn(Optional.of(record("123456", "2147483648", "ACTIVE", 650, 20250510)));

    CustomerInquiryResponse response = service.inquire("123456", "0000000000");

    assertEquals(LookupMode.RANDOM, response.lookupMode());
    assertEquals("Y", response.legacyStatus().inquirySuccess());
    assertEquals("2147483648", response.customer().customerNumber());
    verify(randomCustomerNumberGenerator).nextCustomerNumber(3000000000L);
    }

    @Test
    void randomLookupSkipsInvalidCustomerNumbersWhenFindingUpperBound() {
    when(customerRepository.findLatestBySortCode("123456"))
        .thenReturn(Optional.of(record("123456", "ABC", "INACTIVE", 720, 20260201)));
    when(customerRepository.findBySortCode("123456"))
        .thenReturn(List.of(
            record("123456", "ABC", "INACTIVE", 720, 20260201),
            record("123456", "0000000007", "ACTIVE", 680, 20260110),
            record("123456", "0000000003", "ACTIVE", 650, 20250510)
        ));
    when(randomCustomerNumberGenerator.nextCustomerNumber(7L)).thenReturn("0000000003");
    when(customerRepository.findBySortCodeAndCustomerNumber("123456", "0000000003"))
        .thenReturn(Optional.of(record("123456", "0000000003", "ACTIVE", 650, 20250510)));

    CustomerInquiryResponse response = service.inquire("123456", "0000000000");

    assertEquals(LookupMode.RANDOM, response.lookupMode());
    assertEquals("Y", response.legacyStatus().inquirySuccess());
    assertEquals("0000000003", response.customer().customerNumber());
    verify(randomCustomerNumberGenerator).nextCustomerNumber(7L);
    }

    private static CustomerRecord record(String sortCode, String number, String status, int score, int reviewDate) {
        return new CustomerRecord(
                "CUST",
                sortCode,
                number,
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
                status,
                20100615,
                score,
                reviewDate
        );
    }

}
