package com.bankofz.mainframemodernization.inqstmt.controller;

import com.bankofz.mainframemodernization.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.mainframemodernization.inqacc.logging.CorrelationIdFilter;
import com.bankofz.mainframemodernization.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAuthenticationEntryPoint;
import com.bankofz.mainframemodernization.inqstmt.domain.AccountStatementResponse;
import com.bankofz.mainframemodernization.inqstmt.domain.StatementSummary;
import com.bankofz.mainframemodernization.inqstmt.service.AccountStatementService;
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

@WebMvcTest(controllers = AccountStatementController.class)
@Import({
        AccountStatementExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        CorrelationIdFilter.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
class AccountStatementSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountStatementService accountStatementService;

    @Test
    void shouldReturn401WhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ERR-002"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldReturn403ForInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607")
                        .header("Authorization", "Bearer valid-inqacc-limited-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ERR-003"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void shouldAllowAuthorizedRequest() throws Exception {
        when(accountStatementService.retrieveStatement(any(), any(), any()))
                .thenReturn(new AccountStatementResponse(
                        "123456",
                        "00000001",
                        "202607",
                        new StatementSummary(
                                "20260701",
                                "20260731",
                                new BigDecimal("100.00"),
                                new BigDecimal("10.00"),
                                new BigDecimal("5.00"),
                                new BigDecimal("105.00"),
                                0
                        ),
                        List.of()
                ));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(header().exists("X-Correlation-ID"));
    }
}
