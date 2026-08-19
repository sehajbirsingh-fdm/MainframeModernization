package com.bankofz.mainframemodernization.inqtran.controller;

import com.bankofz.mainframemodernization.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.mainframemodernization.inqacc.logging.CorrelationIdFilter;
import com.bankofz.mainframemodernization.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAuthenticationEntryPoint;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionDetailInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.service.TransactionInquiryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionInquiryController.class)
@Import({
        TransactionInquiryExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        CorrelationIdFilter.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
class TransactionInquirySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionInquiryService transactionInquiryService;

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ERR-002"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldReturn403ForInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions")
                        .header("Authorization", "Bearer valid-inqacc-limited-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ERR-003"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldAllowAuthorizedRequestToReachTransactionInquiryEndpoint() throws Exception {
        when(transactionInquiryService.inquire(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransactionInquiryResponse(
                        "123456",
                        "00000001",
                        null,
                        null,
                        50,
                        0,
                        1,
                        1,
                        List.of(new TransactionRecord(
                                "123456-00000001-20260728-143015-000000000123",
                                "123456",
                                "00000001",
                                "20260728",
                                "143015",
                                "000000000123",
                                "CRD",
                                "Payroll deposit",
                                new BigDecimal("125.50")
                        ))
                ));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.returnedCount").value(1))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldReturn401ForDetailWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ERR-002"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldReturn403ForDetailWithInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123")
                        .header("Authorization", "Bearer valid-inqacc-limited-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ERR-003"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldAllowAuthorizedRequestToReachDetailEndpoint() throws Exception {
        when(transactionInquiryService.inquireDetail(any(), any(), any(), any(), any()))
                .thenReturn(new TransactionDetailInquiryResponse(
                        true,
                        new TransactionRecord(
                                "123456-00000001-20260728-143015-000000000123",
                                "123456",
                                "00000001",
                                "20260728",
                                "143015",
                                "000000000123",
                                "CRD",
                                "Payroll deposit",
                                new BigDecimal("125.50")
                        )
                ));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(header().exists("X-Correlation-ID"));
    }
}
