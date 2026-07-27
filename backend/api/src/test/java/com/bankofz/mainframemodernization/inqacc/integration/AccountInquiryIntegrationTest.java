package com.bankofz.mainframemodernization.inqacc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:inqacc_it;MODE=DB2;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=always"
})
class AccountInquiryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHighestAccountWhenReservedNumberIsUsed() throws Exception {
        mockMvc.perform(get("/v1/accounts/123456/99999999")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.sortcode").value("123456"))
                .andExpect(jsonPath("$.accountNumber").value("00000099"));
    }

    @Test
    void shouldReturnNotFoundForMissingAccount() throws Exception {
        mockMvc.perform(get("/v1/accounts/123456/00000123")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ERR-004"));
    }

    @Test
    void shouldIncludeCorsHeadersForAllowedFrontendOriginOnV1Path() throws Exception {
        mockMvc.perform(get("/v1/accounts/123456/00000001")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
