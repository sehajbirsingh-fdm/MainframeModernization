package com.bankofz.inqcust.api.crecust.controller;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerRequest;
import com.bankofz.inqcust.api.crecust.domain.CreateCustomerResponse;
import com.bankofz.inqcust.api.crecust.domain.DateParts;
import com.bankofz.inqcust.api.crecust.domain.LegacyCreateStatus;
import com.bankofz.inqcust.api.crecust.service.CustomerCreateException;
import com.bankofz.inqcust.api.crecust.service.CustomerCreateService;
import com.bankofz.inqcust.api.inqacc.config.InqaccSecurityConfiguration;
import com.bankofz.inqcust.api.inqacc.security.BearerTokenAuthenticationFilter;
import com.bankofz.inqcust.api.inqacc.security.InqaccAccessDeniedHandler;
import com.bankofz.inqcust.api.inqacc.security.InqaccAuthenticationEntryPoint;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerCreateController.class)
@Import({
        CustomerCreateExceptionHandler.class,
        InqaccSecurityConfiguration.class,
        BearerTokenAuthenticationFilter.class,
        InqaccAuthenticationEntryPoint.class,
        InqaccAccessDeniedHandler.class
})
class CustomerCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerCreateService customerCreateService;

    @Test
    void createReturns201ForValidPayload() throws Exception {
        when(customerCreateService.createCustomer(any())).thenReturn(
                new CreateCustomerResponse(
                        "CUST",
                        "123456",
                        "0000000006",
                        "Mr",
                        "John",
                        "Smith",
                        LocalDate.of(1990, 1, 1),
                        "4165550101",
                        "1 Main",
                        "",
                        "Toronto",
                        "M5H2N2",
                        "Canada",
                        "ACTIVE",
                        LocalDate.of(2026, 7, 22),
                        712,
                        LocalDate.of(2026, 8, 5),
                        new LegacyCreateStatus("Y", " ")
                )
        );

        mockMvc.perform(post("/v1/customers")
                        .header("X-Correlation-ID", "corr-create-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-ID", "corr-create-1"))
                .andExpect(jsonPath("$.customerNumber").value("0000000006"))
                .andExpect(jsonPath("$.legacyStatus.commSuccess").value("Y"));
    }

    @Test
    void invalidPayloadReturns400() throws Exception {
        String invalid = "{}";

        mockMvc.perform(post("/v1/customers")
                        .header("X-Correlation-ID", "corr-create-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-ID", "corr-create-2"))
                .andExpect(jsonPath("$.error.code").value("ERR-001"));
    }

    @Test
    void businessRuleFailureReturns422() throws Exception {
        when(customerCreateService.createCustomer(any())).thenThrow(
                new CustomerCreateException("Invalid title", "ERR-101", "T", HttpStatus.UNPROCESSABLE_ENTITY)
        );

        mockMvc.perform(post("/v1/customers")
                        .header("X-Correlation-ID", "corr-create-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("X-Correlation-ID", "corr-create-3"))
                .andExpect(jsonPath("$.error.code").value("ERR-101"))
                .andExpect(jsonPath("$.error.legacyFailCode").value("T"));
    }

    private CreateCustomerRequest request() {
        return new CreateCustomerRequest(
                "Mr",
                "John",
                "Smith",
                new DateParts(1, 1, 1990),
                new DateParts(22, 7, 2026),
                "4165550101",
                new com.bankofz.inqcust.api.crecust.domain.CreateCustomerAddressRequest("1 Main", "", "Toronto", "M5H2N2", "Canada"),
                "ACTIVE"
        );
    }
}
