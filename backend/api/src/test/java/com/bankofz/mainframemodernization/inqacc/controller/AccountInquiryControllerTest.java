package com.bankofz.mainframemodernization.inqacc.controller;

import com.bankofz.mainframemodernization.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.mainframemodernization.inqacc.domain.AccountResponse;
import com.bankofz.mainframemodernization.inqacc.exception.AccountNotFoundException;
import com.bankofz.mainframemodernization.inqacc.exception.RepositoryUnavailableException;
import com.bankofz.mainframemodernization.inqacc.logging.CorrelationIdFilter;
import com.bankofz.mainframemodernization.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAuthenticationEntryPoint;
import com.bankofz.mainframemodernization.inqacc.service.AccountInquiryService;
import com.bankofz.mainframemodernization.inqacc.validation.AccountInquiryValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountInquiryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AccountInquiryValidator.class
        )
)
@Import({
        AccountInquiryExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        CorrelationIdFilter.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
@ExtendWith(OutputCaptureExtension.class)
class AccountInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountInquiryService accountInquiryService;

    @Test
    void shouldReturn200ForSuccessfulInquiry() throws Exception {
        when(accountInquiryService.inquireAccount("543210", "12345678"))
                .thenReturn(new AccountResponse(
                        "ACCOUNT",
                        "1000000001",
                        "543210",
                        "12345678",
                        "CHK",
                        new BigDecimal("1.25"),
                        "2023-01-10",
                        1500,
                        "2024-04-01",
                        "2024-05-01",
                        new BigDecimal("1800.00"),
                        new BigDecimal("1700.00")
                ));

        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortcode").value("543210"))
                .andExpect(jsonPath("$.accountNumber").value("12345678"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldReturn400ForValidationFailure() throws Exception {
        doThrow(new IllegalArgumentException("sortcode must be exactly 6 numeric digits"))
                .when(accountInquiryService)
                .inquireAccount(anyString(), anyString());

        mockMvc.perform(get("/v1/accounts/999/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ERR-001"));
    }

    @Test
    void shouldReturn404WhenAccountIsMissing() throws Exception {
        doThrow(new AccountNotFoundException("Account record not found"))
                .when(accountInquiryService)
                .inquireAccount("543210", "12345678");

        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ERR-004"));
    }

    @Test
    void shouldReturn503WhenRepositoryIsUnavailable() throws Exception {
        doThrow(new RepositoryUnavailableException("Failed JDBC account lookup", new RuntimeException("down")))
                .when(accountInquiryService)
                .inquireAccount("543210", "12345678");

        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("ERR-005"));
    }

    @Test
    void shouldReturn401WithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/v1/accounts/543210/12345678"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error.code").value("ERR-002"));
    }

    @Test
    void shouldReturn401ForMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Token abc"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ERR-002"));
    }

    @Test
    void shouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ERR-002"));
    }

    @Test
    void shouldReturn403ForInsufficientRole() throws Exception {
        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-limited-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ERR-003"));
    }

    @Test
    void shouldReturn500ForUnexpectedFailure() throws Exception {
        doThrow(new RuntimeException("unexpected"))
                .when(accountInquiryService)
                .inquireAccount("543210", "12345678");

        mockMvc.perform(get("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("ERR-006"));
    }

    @Test
    void shouldPropagateCorrelationIdToErrorEnvelopeAndHeader() throws Exception {
        doThrow(new AccountNotFoundException("Account record not found"))
                .when(accountInquiryService)
                .inquireAccount("543210", "12345678");

        mockMvc.perform(get("/v1/accounts/543210/12345678")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token")
                        .header("X-Correlation-ID", "corr-123"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Correlation-ID", "corr-123"))
                .andExpect(jsonPath("$.error.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.error.timestamp").isNotEmpty());
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        doThrow(new IllegalArgumentException("sortcode must be exactly 6 numeric digits"))
                .when(accountInquiryService)
                .inquireAccount(anyString(), anyString());

        mockMvc.perform(get("/v1/accounts/999/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.error.correlationId").isNotEmpty());
    }

    @Test
    void shouldRejectWriteOperationForReadOnlyBoundary() throws Exception {
        mockMvc.perform(post("/v1/accounts/543210/12345678").header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLogOnlySafeStructuredMetadata(CapturedOutput output) throws Exception {
        when(accountInquiryService.inquireAccount("543210", "12345678"))
                .thenReturn(new AccountResponse(
                        "ACCOUNT",
                        "1000000001",
                        "543210",
                        "12345678",
                        "CHK",
                        new BigDecimal("1.25"),
                        "2023-01-10",
                        1500,
                        "2024-04-01",
                        "2024-05-01",
                        new BigDecimal("1800.00"),
                        new BigDecimal("1700.00")
                ));

        mockMvc.perform(get("/v1/accounts/543210/12345678")
                .header("Authorization", "Bearer valid-inqacc-inquirer-token"));

        String logs = output.getOut();
        org.assertj.core.api.Assertions.assertThat(logs).contains("event=inqacc_inquiry_success");
        org.assertj.core.api.Assertions.assertThat(logs).doesNotContain("valid-inqacc-inquirer-token");
    }
}
