package com.bankofz.inqcust.api.inqacc.contract;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.data.mode=mock",
        "app.inqacc.mock-data.path=mock-data/account-records.json"
})
class InqaccOpenApiConformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contractFileShouldContainRequiredPathAndStatuses() throws Exception {
        Path contractPath = Path.of("..", "..", "specs", "002-inqacc-account-inquiry-modernization", "contracts", "openapi.yaml");
        String contract = Files.readString(contractPath);

        assertThat(contract).contains("/accounts/{sortcode}/{accountNumber}");
        assertThat(contract).contains("'200':");
        assertThat(contract).contains("'400':");
        assertThat(contract).contains("'401':");
        assertThat(contract).contains("'403':");
        assertThat(contract).contains("'404':");
        assertThat(contract).contains("'500':");
        assertThat(contract).contains("'503':");
    }

    @Test
    void successPayloadShouldExposeAllTwelveRequiredFields() throws Exception {
        mockMvc.perform(get("/v1/accounts/123456/00000001")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.eyecatcher").isNotEmpty())
                .andExpect(jsonPath("$.customerNumber").isNotEmpty())
                .andExpect(jsonPath("$.sortcode").isNotEmpty())
                .andExpect(jsonPath("$.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.accountType").isNotEmpty())
                .andExpect(jsonPath("$.interestRate").isNumber())
                .andExpect(jsonPath("$.accountOpened").isNotEmpty())
                .andExpect(jsonPath("$.overdraftLimit").isNumber())
                .andExpect(jsonPath("$.lastStatementDate").isNotEmpty())
                .andExpect(jsonPath("$.nextStatementDate").isNotEmpty())
                .andExpect(jsonPath("$.availableBalance").isNumber())
                .andExpect(jsonPath("$.actualBalance").isNumber());
    }

    @Test
    void errorPayloadShouldMatchCanonicalEnvelopeShape() throws Exception {
        mockMvc.perform(get("/v1/accounts/12/123")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.error.code").value("ERR-001"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.error.correlationId").isNotEmpty());
    }
}
