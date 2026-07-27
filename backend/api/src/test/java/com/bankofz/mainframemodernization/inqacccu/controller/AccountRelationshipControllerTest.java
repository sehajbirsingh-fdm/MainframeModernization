package com.bankofz.mainframemodernization.inqacccu.controller;

import com.bankofz.mainframemodernization.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.mainframemodernization.inqacccu.domain.AccountSummary;
import com.bankofz.mainframemodernization.inqacccu.domain.LegacyStatus;
import com.bankofz.mainframemodernization.inqacccu.exception.RepositoryUnavailableException;
import com.bankofz.mainframemodernization.inqacccu.service.AccountRelationshipService;
import com.bankofz.mainframemodernization.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.mainframemodernization.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAuthenticationEntryPoint;
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

        private static AccountRelationshipResponse businessFailureResponse(String customerNumber, String failCode, String customerFound) {
                return new AccountRelationshipResponse(
                                new LegacyStatus("N", failCode, customerFound),
                                customerNumber,
                                0,
                                List.of()
                );
        }

    @Test
    void shouldReturnSuccessPayload() throws Exception {
        AccountRelationshipResponse response = new AccountRelationshipResponse(
                new LegacyStatus("Y", "0", "Y"),
                "0000000001",
                1,
                List.of(new AccountSummary(
                        "ACCT",
                        "0000000001",
                        "123456",
                        "1000000001",
                        "CHK",
                        new BigDecimal("0.50"),
                        "2020-01-15",
                        500,
                        "2025-12-31",
                        "2026-01-31",
                        new BigDecimal("1520.45"),
                        new BigDecimal("1498.12")
                ))
        );

        when(service.inquire("0000000001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/0000000001/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyStatus.success").value("Y"))
                .andExpect(jsonPath("$.legacyStatus.failCode").value("0"))
                .andExpect(jsonPath("$.customerNumber").value("0000000001"))
                .andExpect(jsonPath("$.numberOfAccounts").value(1));
    }

    @Test
    void shouldReturnBadRequestForInvalidCustomerNumber() throws Exception {
        mockMvc.perform(get("/api/v1/customers/ABC/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details[0].field").value("customerNumber"))
                .andExpect(jsonPath("$.error.details[0].reason").exists());
    }

        @Test
        void shouldReturnBusinessNotFoundPayloadForFailCode1() throws Exception {
                when(service.inquire("0000000999")).thenReturn(businessFailureResponse("0000000999", "1", "N"));

                mockMvc.perform(get("/api/v1/customers/0000000999/accounts").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.legacyStatus.success").value("N"))
                                .andExpect(jsonPath("$.legacyStatus.failCode").value("1"))
                                .andExpect(jsonPath("$.legacyStatus.customerFound").value("N"))
                                .andExpect(jsonPath("$.customerNumber").value("0000000999"))
                                .andExpect(jsonPath("$.numberOfAccounts").value(0))
                                .andExpect(jsonPath("$.accounts").isArray())
                                .andExpect(jsonPath("$.accounts").isEmpty());
        }

        @Test
        void shouldReturnBusinessRetrievalOpenFailurePayloadForFailCode2() throws Exception {
                when(service.inquire("0000000200")).thenReturn(businessFailureResponse("0000000200", "2", "Y"));

                mockMvc.perform(get("/api/v1/customers/0000000200/accounts").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.legacyStatus.success").value("N"))
                                .andExpect(jsonPath("$.legacyStatus.failCode").value("2"))
                                .andExpect(jsonPath("$.legacyStatus.customerFound").value("Y"))
                                .andExpect(jsonPath("$.customerNumber").value("0000000200"))
                                .andExpect(jsonPath("$.numberOfAccounts").value(0))
                                .andExpect(jsonPath("$.accounts").isArray())
                                .andExpect(jsonPath("$.accounts").isEmpty());
        }

        @Test
        void shouldReturnBusinessRetrievalFetchFailurePayloadForFailCode3() throws Exception {
                when(service.inquire("0000000300")).thenReturn(businessFailureResponse("0000000300", "3", "Y"));

                mockMvc.perform(get("/api/v1/customers/0000000300/accounts").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.legacyStatus.success").value("N"))
                                .andExpect(jsonPath("$.legacyStatus.failCode").value("3"))
                                .andExpect(jsonPath("$.legacyStatus.customerFound").value("Y"))
                                .andExpect(jsonPath("$.customerNumber").value("0000000300"))
                                .andExpect(jsonPath("$.numberOfAccounts").value(0))
                                .andExpect(jsonPath("$.accounts").isArray())
                                .andExpect(jsonPath("$.accounts").isEmpty());
        }

        @Test
        void shouldReturnBusinessRetrievalCloseFailurePayloadForFailCode4() throws Exception {
                when(service.inquire("0000000400")).thenReturn(businessFailureResponse("0000000400", "4", "Y"));

                mockMvc.perform(get("/api/v1/customers/0000000400/accounts").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.legacyStatus.success").value("N"))
                                .andExpect(jsonPath("$.legacyStatus.failCode").value("4"))
                                .andExpect(jsonPath("$.legacyStatus.customerFound").value("Y"))
                                .andExpect(jsonPath("$.customerNumber").value("0000000400"))
                                .andExpect(jsonPath("$.numberOfAccounts").value(0))
                                .andExpect(jsonPath("$.accounts").isArray())
                                .andExpect(jsonPath("$.accounts").isEmpty());
        }

    @Test
    void shouldReturnInternalErrorForRepositoryFailure() throws Exception {
        when(service.inquire("0000000001")).thenThrow(new RepositoryUnavailableException("boom", new RuntimeException()));

        mockMvc.perform(get("/api/v1/customers/0000000001/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.type").value("INFRASTRUCTURE_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Service unavailable due to infrastructure failure"));
    }
}
