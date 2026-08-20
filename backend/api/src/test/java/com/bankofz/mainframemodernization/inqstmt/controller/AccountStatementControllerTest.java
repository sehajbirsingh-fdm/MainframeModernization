package com.bankofz.mainframemodernization.inqstmt.controller;

import com.bankofz.mainframemodernization.inqstmt.domain.AccountStatementResponse;
import com.bankofz.mainframemodernization.inqstmt.domain.StatementEntry;
import com.bankofz.mainframemodernization.inqstmt.domain.StatementSummary;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementNotFoundException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementTechnicalException;
import com.bankofz.mainframemodernization.inqstmt.service.AccountStatementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountStatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountStatementService accountStatementService;

    @Test
    void shouldReturn200ForValidRequest() throws Exception {
        AccountStatementResponse response = new AccountStatementResponse(
                "123456",
                "00000001",
                "202607",
                new StatementSummary(
                        "20260701",
                        "20260731",
                        new BigDecimal("500.00"),
                        new BigDecimal("125.50"),
                        new BigDecimal("45.75"),
                        new BigDecimal("579.75"),
                        1
                ),
                List.of(new StatementEntry(
                        "20260728",
                        "143015",
                        "000000000123",
                        "CRD",
                        "Payroll deposit",
                        new BigDecimal("125.50")
                ))
        );

        when(accountStatementService.retrieveStatement("123456", "00000001", "202607")).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.summary.periodFrom").value("20260701"))
                .andExpect(jsonPath("$.entries[0].reference").value("000000000123"));
    }

    @Test
    void shouldReturn400ForInvalidPeriodMonth() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202613"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-001"));
    }

    @Test
    void shouldReturn404WhenAccountMissing() throws Exception {
        when(accountStatementService.retrieveStatement(any(), any(), any()))
                .thenThrow(new StatementNotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/123456/00000009/statements/202607"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-404"));
    }

    @Test
    void shouldReturn500ForTechnicalFailure() throws Exception {
        when(accountStatementService.retrieveStatement(any(), any(), any()))
                .thenThrow(new StatementTechnicalException("failed", new RuntimeException("db down")));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERR-500"));
    }
}
