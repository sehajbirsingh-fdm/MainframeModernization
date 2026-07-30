package com.bankofz.mainframemodernization.updcust.controller;

import com.bankofz.mainframemodernization.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.mainframemodernization.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.mainframemodernization.inqacc.security.InqaccAuthenticationEntryPoint;
import com.bankofz.mainframemodernization.updcust.domain.LegacyUpdateStatus;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerAddressRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerRequest;
import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerResponse;
import com.bankofz.mainframemodernization.updcust.service.UpdateCustomerException;
import com.bankofz.mainframemodernization.updcust.service.UpdateCustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UpdateCustomerController.class)
@Import({
        UpdateCustomerExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
class UpdateCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpdateCustomerService updateCustomerService;

    @Test
    void updateReturns200ForValidPayload() throws Exception {
        when(updateCustomerService.updateCustomer(eq("123456"), eq("1"), any()))
                .thenReturn(successResponse());

        mockMvc.perform(put("/api/v1/customers/1")
                        .queryParam("sortCode", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("0000000001"))
                .andExpect(jsonPath("$.legacyStatus.updSuccess").value("Y"));
    }

    @Test
    void invalidPathVariableReturns400() throws Exception {
        mockMvc.perform(put("/api/v1/customers/ABC")
                        .queryParam("sortCode", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UPDCUST-400-VALIDATION"));
    }

    @Test
    void businessRuleFailureReturns422() throws Exception {
        when(updateCustomerService.updateCustomer(eq("123456"), eq("1"), any()))
                .thenThrow(new UpdateCustomerException("Invalid title", "UPDCUST-422-TITLE", "T", HttpStatus.UNPROCESSABLE_ENTITY));

        mockMvc.perform(put("/api/v1/customers/1")
                        .queryParam("sortCode", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("UPDCUST-422-TITLE"))
                .andExpect(jsonPath("$.error.legacyFailCode").value("T"));
    }

    @Test
    void notFoundReturns404() throws Exception {
        when(updateCustomerService.updateCustomer(eq("123456"), eq("1"), any()))
                .thenThrow(new UpdateCustomerException("Customer not found", "UPDCUST-404", "1", HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/v1/customers/1")
                        .queryParam("sortCode", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.legacyFailCode").value("1"));
    }

    @Test
    void internalFailureReturns500() throws Exception {
        when(updateCustomerService.updateCustomer(eq("123456"), eq("1"), any()))
                .thenThrow(new UpdateCustomerException("Update failed", "UPDCUST-500-UPDATE", "3", HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(put("/api/v1/customers/1")
                        .queryParam("sortCode", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.legacyFailCode").value("3"));
    }

    private UpdateCustomerRequest request() {
        return new UpdateCustomerRequest(
                "Ms",
                "Jane",
                "Doe",
                "1990-01-01",
                "4165550111",
                new UpdateCustomerAddressRequest("10 Bay Street", "Suite 200", "Toronto", "M5J2N8", "Canada"),
                "ACTIVE"
        );
    }

    private UpdateCustomerResponse successResponse() {
        return new UpdateCustomerResponse(
                "0000000001",
                "123456",
                "Ms",
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "4165550111",
                new UpdateCustomerAddressRequest("10 Bay Street", "Suite 200", "Toronto", "M5J2N8", "Canada"),
                "ACTIVE",
                LocalDate.of(2010, 6, 15),
                742,
                LocalDate.of(2026, 1, 15),
                new LegacyUpdateStatus("Y", " ")
        );
    }
}
