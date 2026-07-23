package com.bankofz.inqcust.api.inqacccu.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.data.mode=mock",
        "app.inqacccu.mock-data.path=mock-data/account-relationship-records.json"
})
class InqacccuOpenApiConformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contractFileShouldContainRequiredPathAndStatuses() throws Exception {
        Path contractPath = Path.of("..", "..", "specs", "003-inqacccu-customer-account-relationship-modernization", "contracts", "openapi.yaml");
        String contract = Files.readString(contractPath);

        assertThat(contract).contains("/api/v1/customers/{customerNumber}/accounts");
        assertThat(contract).contains("'200':");
        assertThat(contract).contains("'400':");
        assertThat(contract).contains("'500':");
    }

    @Test
    void successPayloadShouldExposeRequiredShapes() throws Exception {
        mockMvc.perform(get("/api/v1/customers/0000000001/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyStatus.success").value("Y"))
                .andExpect(jsonPath("$.legacyStatus.failCode").value("0000"))
                .andExpect(jsonPath("$.customer.customerNumber").value("0000000001"))
                .andExpect(jsonPath("$.accounts.count").value(2))
                .andExpect(jsonPath("$.accounts.accounts[0].accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.accounts.accounts[0].lastStatementDate").value("2025-12-31"));
    }

    @Test
    void businessNotFoundShouldReturn200WithLegacyStatusN() throws Exception {
        mockMvc.perform(get("/api/v1/customers/0000000999/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyStatus.success").value("N"))
                .andExpect(jsonPath("$.legacyStatus.failCode").value("1001"))
            .andExpect(jsonPath("$.customer").isEmpty())
            .andExpect(jsonPath("$.accounts").isEmpty());
    }

    @Test
    void invalidInputShouldReturnValidationErrorShape() throws Exception {
        mockMvc.perform(get("/api/v1/customers/ABC/accounts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-001"))
                .andExpect(jsonPath("$.message").value("Invalid customer number format"));
    }
}
