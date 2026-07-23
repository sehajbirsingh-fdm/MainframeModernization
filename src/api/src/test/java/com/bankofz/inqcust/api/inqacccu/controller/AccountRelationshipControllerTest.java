package com.bankofz.inqcust.api.inqacccu.controller;

import com.bankofz.inqcust.api.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.inqcust.api.inqacccu.domain.AccountSummary;
import com.bankofz.inqcust.api.inqacccu.domain.AccountsList;
import com.bankofz.inqcust.api.inqacccu.domain.CustomerSummary;
import com.bankofz.inqcust.api.inqacccu.domain.LegacyStatus;
import com.bankofz.inqcust.api.inqacccu.exception.RepositoryUnavailableException;
import com.bankofz.inqcust.api.inqacccu.service.AccountRelationshipService;
import com.bankofz.inqcust.api.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.inqcust.api.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.inqcust.api.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.inqcust.api.inqacc.security.InqaccAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountRelationshipController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        AccountRelationshipExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
class AccountRelationshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRelationshipService service;

    @Test
    void shouldReturnSuccessPayload() throws Exception {
        AccountRelationshipResponse response = new AccountRelationshipResponse(
                new LegacyStatus("Y", "0000", "Y"),
                new CustomerSummary("0000000001", "John Smith", "123456", "INDIVIDUAL"),
                new AccountsList(
                        1,
                        List.of(new AccountSummary(
                                "1000000001",
                                "123456",
                                "CHK",
                                "Checking Account",
                                new BigDecimal("1520.45"),
                                "GBP",
                                new BigDecimal("1498.12"),
                                "GBP",
                                new BigDecimal("0.50"),
                                500,
                                "2025-12-31",
                                "2026-01-31"
                        ))
                )
        );

        when(service.inquire("0000000001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/0000000001/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyStatus.success").value("Y"))
                .andExpect(jsonPath("$.legacyStatus.failCode").value("0000"))
                .andExpect(jsonPath("$.customer.customerName").value("John Smith"))
                .andExpect(jsonPath("$.accounts.count").value(1));
    }

    @Test
    void shouldReturnBadRequestForInvalidCustomerNumber() throws Exception {
        mockMvc.perform(get("/api/v1/customers/ABC/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-001"));
    }

    @Test
    void shouldReturnInternalErrorForRepositoryFailure() throws Exception {
        when(service.inquire("0000000001")).thenThrow(new RepositoryUnavailableException("boom", new RuntimeException()));

        mockMvc.perform(get("/api/v1/customers/0000000001/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERR-005"))
                .andExpect(jsonPath("$.message").value("Internal processing error"));
    }
}
