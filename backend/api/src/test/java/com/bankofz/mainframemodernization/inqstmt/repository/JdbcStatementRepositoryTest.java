package com.bankofz.mainframemodernization.inqstmt.repository;

import com.bankofz.mainframemodernization.inqstmt.repository.model.StatementEntryRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inqstmt_repo;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class JdbcStatementRepositoryTest {

    @Autowired
    private StatementRepository statementRepository;

    @Test
    void shouldApplySingleAccountFilteringAndAscendingOrder() {
        List<StatementEntryRow> rows = statementRepository.findEntriesWithinPeriod(
                "123456",
                "00000001",
                "20260701",
                "20260731"
        );

        assertThat(rows).hasSize(5);
        assertThat(rows).allMatch(row -> row.reference() != null);
        assertThat(rows.get(0).reference()).isEqualTo("000000000126");
        assertThat(rows.get(1).reference()).isEqualTo("000000000127");
        assertThat(rows.get(4).reference()).isEqualTo("000000000123");
    }

    @Test
    void shouldComputeHistoricalSumBeforePeriod() {
        BigDecimal prePeriodTotal = statementRepository.sumAmountsBeforeDate("123456", "00000001", "20260727");

        assertThat(prePeriodTotal).isEqualByComparingTo("5.25");
    }

    @Test
    void shouldReturnFalseWhenAccountMissing() {
        assertThat(statementRepository.accountExists("123456", "00999999")).isFalse();
    }
}
