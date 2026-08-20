package com.bankofz.mainframemodernization.inqtran.controller;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionDetailInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionTechnicalException;
import com.bankofz.mainframemodernization.inqtran.service.TransactionInquiryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionInquiryService transactionInquiryService;

    @Test
    void shouldReturn200ForPopulatedSuccess() throws Exception {
        when(transactionInquiryService.inquire(eq("123456"), eq("00000001"), eq("20260701"), eq("20260731"), eq(2), eq(0)))
                .thenReturn(new TransactionInquiryResponse(
                        "123456",
                        "00000001",
                        "20260701",
                        "20260731",
                        2,
                        0,
                        5,
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
                        .param("fromDate", "20260701")
                        .param("toDate", "20260731")
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.returnedCount").value(1))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("123456-00000001-20260728-143015-000000000123"));
    }

    @Test
    void shouldReturn200ForEmptySuccess() throws Exception {
        when(transactionInquiryService.inquire(eq("123456"), eq("00000001"), any(), any(), any(), any()))
                .thenReturn(new TransactionInquiryResponse("123456", "00000001", null, null, 50, 0, 0, 0, List.of()));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test
    void shouldReturn400ForInvalidPathOrQueryValues() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123/00000001/transactions").param("limit", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-001"));
    }

    @Test
    void shouldReturn500ForTechnicalFailure() throws Exception {
        when(transactionInquiryService.inquire(any(), any(), any(), any(), any(), any()))
                .thenThrow(new TransactionTechnicalException("failed", new RuntimeException("db down")));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERR-500"));
    }

    @Test
    void shouldReturn200FoundForDetailEndpoint() throws Exception {
        when(transactionInquiryService.inquireDetail("123456", "00000001", "20260728", "143015", "000000000123"))
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

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.transaction.transactionId").value("123456-00000001-20260728-143015-000000000123"));
    }

    @Test
    void shouldReturn200NotFoundForDetailEndpoint() throws Exception {
        when(transactionInquiryService.inquireDetail("123456", "00000001", "20990101", "120000", "999999999999"))
                .thenReturn(new TransactionDetailInquiryResponse(false, null));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20990101/120000/999999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.transaction").isEmpty());
    }

    @Test
    void shouldReturn400ForInvalidDetailPathValues() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/12345/00000001/transactions/20260728/143015/000000000123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-001"));
    }

    @Test
    void shouldReturn500ForDetailTechnicalFailure() throws Exception {
        when(transactionInquiryService.inquireDetail(any(), any(), any(), any(), any()))
                .thenThrow(new TransactionTechnicalException("failed", new RuntimeException("db down")));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERR-500"));
    }
}
