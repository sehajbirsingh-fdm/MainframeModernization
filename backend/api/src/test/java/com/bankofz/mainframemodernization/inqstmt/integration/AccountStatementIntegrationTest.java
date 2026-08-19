package com.bankofz.mainframemodernization.inqstmt.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inqstmt_integration;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class AccountStatementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200WhenCustomerRowMissingButAccountAndTransactionsExist() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000077/statements/202607")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.accountNumber").value("00000077"))
                .andExpect(jsonPath("$.summary.transactionCount").value(1));
    }

    @Test
    void shouldIncludeLeapDayEntriesForLeapYearFebruary() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000077/statements/202802")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.periodTo").value("20280229"))
                .andExpect(jsonPath("$.entries[0].date").value("20280229"));
    }
}
