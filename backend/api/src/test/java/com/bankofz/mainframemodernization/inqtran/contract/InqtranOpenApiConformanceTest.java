package com.bankofz.mainframemodernization.inqtran.contract;

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
        "spring.datasource.url=jdbc:h2:mem:inqtran_contract;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class InqtranOpenApiConformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void featureContractShouldContainPathAndStatuses() throws Exception {
        Path contractPath = Path.of("..", "..", "specs", "005-inqtran-transaction-inquiry-modernization", "contracts", "openapi.yaml");
        String contract = Files.readString(contractPath);

        assertThat(contract).contains("/api/v1/accounts/{sortCode}/{accountNumber}/transactions");
        assertThat(contract).contains("'200':");
        assertThat(contract).contains("'400':");
        assertThat(contract).contains("'500':");
    }

    @Test
    void runtimeOpenApiPublicationShouldContainInqtranPath() throws Exception {
        Path runtimePath = Path.of("src", "main", "resources", "openapi.yaml");
        String runtimeOpenApi = Files.readString(runtimePath);

        assertThat(runtimeOpenApi).contains("/api/v1/accounts/{sortCode}/{accountNumber}/transactions");
        assertThat(runtimeOpenApi).contains("InqtranTransactionListResponse");
    }

    @Test
    void successPayloadShouldExposeApprovedTransactionListFields() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.accountNumber").value("00000001"))
                .andExpect(jsonPath("$.limit").value(50))
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.returnedCount").isNumber())
                .andExpect(jsonPath("$.transactions").isArray());
    }
}
