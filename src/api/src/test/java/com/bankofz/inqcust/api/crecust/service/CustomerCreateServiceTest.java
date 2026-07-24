package com.bankofz.inqcust.api.crecust.service;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerAddressRequest;
import com.bankofz.inqcust.api.crecust.domain.CreateCustomerRequest;
import com.bankofz.inqcust.api.crecust.domain.CreateCustomerResponse;
import com.bankofz.inqcust.api.crecust.domain.DateParts;
import com.bankofz.inqcust.api.crecust.repository.CustomerCreateRepository;
import com.bankofz.inqcust.api.domain.CustomerRecord;
import com.bankofz.inqcust.api.service.LegacyDateConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class CustomerCreateServiceTest {

    private CustomerCreateRepository repository;
    private CreditCheckGateway creditCheckGateway;
    private CustomerCreateService service;

    @BeforeEach
    void setUp() {
        repository = mock(CustomerCreateRepository.class);
        creditCheckGateway = mock(CreditCheckGateway.class);
        service = new CustomerCreateService(repository, creditCheckGateway, new LegacyDateConverter(), "123456");
    }

    @Test
    void createsCustomerForValidRequest() {
        CreateCustomerRequest request = request("Mr", "John", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );
        when(repository.nextCustomerNumber("123456")).thenReturn(6L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerResponse response = service.createCustomer(request);

        assertEquals("CUST", response.eyecatcher());
        assertEquals("123456", response.sortCode());
        assertEquals("0000000006", response.customerNumber());
        assertEquals("Y", response.legacyStatus().commSuccess());
        assertEquals(" ", response.legacyStatus().commFailCode());
        assertEquals(712, response.creditScore());
    }

    @Test
    void rejectsInvalidTitleWithLegacyFailCodeT() {
        CreateCustomerRequest request = request("Captain", "John", "Smith");

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("T", exception.legacyFailCode());
        assertEquals("ERR-101", exception.errorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void rejectsFutureDobWithLegacyFailCodeY() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Mr",
                "John",
                "Smith",
                new DateParts(1, 1, 2999),
                new DateParts(22, 7, 2026),
                "4165550101",
                new CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("Y", exception.legacyFailCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void creditCheckFailureReturnsLegacyFailCodeG() {
        CreateCustomerRequest request = request("Mr", "NoCreditUser", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(false, 0, LocalDate.of(2026, 7, 23), "G")
        );

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("G", exception.legacyFailCode());
        assertEquals("ERR-301", exception.errorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void rejectsDobYearBelow1601WithLegacyFailCodeO() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Mr",
                "John",
                "Smith",
                new DateParts(1, 1, 1500),
                new DateParts(22, 7, 2026),
                "4165550101",
                new CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("O", exception.legacyFailCode());
        assertEquals("ERR-102", exception.errorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void rejectsInvalidCalendarDobWithLegacyFailCodeZ() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Mr",
                "John",
                "Smith",
                new DateParts(31, 2, 1990),
                new DateParts(22, 7, 2026),
                "4165550101",
                new CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("Z", exception.legacyFailCode());
        assertEquals("ERR-103", exception.errorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void rejectsOver150YearsDobWithLegacyFailCodeO() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Mr",
                "John",
                "Smith",
                new DateParts(1, 1, 1800),
                new DateParts(22, 7, 2026),
                "4165550101",
                new CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("O", exception.legacyFailCode());
        assertEquals("ERR-102", exception.errorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
    }

    @Test
    void creditFailureMappingsSupportLegacyFailCodesAThroughH() {
        String[] failCodes = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (String failCode : failCodes) {
            when(creditCheckGateway.assess(any(), any())).thenReturn(
                    new CreditCheckGateway.CreditCheckResult(false, 0, LocalDate.of(2026, 7, 23), failCode)
            );

            CustomerCreateException exception = assertThrows(CustomerCreateException.class,
                    () -> service.createCustomer(request("Mr", "John", "Smith")));

            assertEquals(failCode, exception.legacyFailCode());
            assertEquals("ERR-301", exception.errorCode());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus());
        }
    }

    @Test
    void mapsPersistenceFailureToLegacyFailCode1And503() {
        CreateCustomerRequest request = request("Mr", "John", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );
        when(repository.nextCustomerNumber("123456")).thenReturn(6L);
        doThrow(new RuntimeException("write failure")).when(repository).save(any());

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("1", exception.legacyFailCode());
        assertEquals("ERR-201", exception.errorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.httpStatus());
    }

    @Test
    void mapsControlStateFailureToLegacyFailCode4And503() {
        CreateCustomerRequest request = request("Mr", "John", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );
        when(repository.nextCustomerNumber("123456")).thenThrow(new RuntimeException("control failure"));

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("4", exception.legacyFailCode());
        assertEquals("ERR-203", exception.errorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.httpStatus());
    }

    @Test
    void mapsLockAcquireFailureToLegacyFailCode3And503() throws Exception {
        CreateCustomerRequest request = request("Mr", "John", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );

        setLock("123456", new TryLockFailingLock());

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));
        assertEquals("3", exception.legacyFailCode());
        assertEquals("ERR-202", exception.errorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.httpStatus());
    }

    @Test
    void mapsLockReleaseFailureToLegacyFailCode5And503() throws Exception {
        CreateCustomerRequest request = request("Mr", "John", "Smith");
        when(creditCheckGateway.assess(any(), any())).thenReturn(
                new CreditCheckGateway.CreditCheckResult(true, 712, LocalDate.of(2026, 8, 5), " ")
        );
        when(repository.nextCustomerNumber("123456")).thenReturn(6L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        setLock("123456", new UnlockFailingLock());

        CustomerCreateException exception = assertThrows(CustomerCreateException.class, () -> service.createCustomer(request));

        assertEquals("5", exception.legacyFailCode());
        assertEquals("ERR-204", exception.errorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.httpStatus());
    }

    @SuppressWarnings("unchecked")
    private void setLock(String sortCode, ReentrantLock lock) throws Exception {
        Field field = CustomerCreateService.class.getDeclaredField("sortCodeLocks");
        field.setAccessible(true);
        ConcurrentMap<String, ReentrantLock> locks = (ConcurrentMap<String, ReentrantLock>) field.get(service);
        locks.put(sortCode, lock);
    }

    private static final class UnlockFailingLock extends ReentrantLock {
        @Override
        public void unlock() {
            throw new IllegalMonitorStateException("forced unlock failure");
        }
    }

    private static final class TryLockFailingLock extends ReentrantLock {
        @Override
        public boolean tryLock() {
            return false;
        }
    }

    private CreateCustomerRequest request(String title, String firstName, String lastName) {
        return new CreateCustomerRequest(
                title,
                firstName,
                lastName,
                new DateParts(1, 1, 1990),
                new DateParts(22, 7, 2026),
                "4165550101",
                new CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
    }
}
