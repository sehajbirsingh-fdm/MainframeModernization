package com.bankofz.mainframemodernization.inqtran.repository;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inqtran_repo;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class JdbcTransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldApplyExactAccountAndInclusiveDateFiltering() {
        TransactionQueryCriteria criteria = new TransactionQueryCriteria(
                "123456",
                "00000001",
                "20260727",
                "20260728",
                100,
                0
        );

        int count = transactionRepository.countByCriteria(criteria);
        List<TransactionRow> rows = transactionRepository.findByCriteria(criteria);

        assertThat(count).isEqualTo(3);
        assertThat(rows).hasSize(3);
        assertThat(rows).allMatch(row -> row.sortCode().equals("123456") && row.accountNumber().equals("00000001"));
    }

    @Test
    void shouldOrderByDateDescThenTimeDescBeforePagination() {
        TransactionQueryCriteria criteria = new TransactionQueryCriteria(
                "123456",
                "00000001",
                "00000000",
                "99999999",
                3,
                0
        );

        List<TransactionRow> rows = transactionRepository.findByCriteria(criteria);

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).reference()).isEqualTo("000000000123");
        assertThat(rows.get(1).reference()).isEqualTo("000000000124");
        assertThat(rows.get(2).reference()).isEqualTo("000000000125");
    }

    @Test
    void shouldReturnEmptyRowsForOffsetBeyondFilteredTotal() {
        TransactionQueryCriteria criteria = new TransactionQueryCriteria(
                "123456",
                "00000001",
                "00000000",
                "99999999",
                50,
                99
        );

        int count = transactionRepository.countByCriteria(criteria);
        List<TransactionRow> rows = transactionRepository.findByCriteria(criteria);

        assertThat(count).isEqualTo(5);
        assertThat(rows).isEmpty();
    }

    @Test
    void shouldReturnFoundDetailForExactFivePartIdentity() {
        Optional<TransactionRow> row = transactionRepository.findDetailByIdentity(
                "123456",
                "00000001",
                "20260728",
                "143015",
                "000000000123"
        );

        assertThat(row).isPresent();
        assertThat(row.get().sortCode()).isEqualTo("123456");
        assertThat(row.get().accountNumber()).isEqualTo("00000001");
        assertThat(row.get().date()).isEqualTo("20260728");
        assertThat(row.get().time()).isEqualTo("143015");
        assertThat(row.get().reference()).isEqualTo("000000000123");
    }

    @Test
    void shouldReturnEmptyDetailForNonMatchingIdentity() {
        Optional<TransactionRow> row = transactionRepository.findDetailByIdentity(
                "123456",
                "00000001",
                "20990101",
                "010101",
                "999999999999"
        );

        assertThat(row).isEmpty();
    }

    @Test
    void shouldPreserveLeadingZerosForDetailIdentity() {
        Optional<TransactionRow> row = transactionRepository.findDetailByIdentity(
                "123456",
                "00000001",
                "20260726",
                "223000",
                "000000000127"
        );

        assertThat(row).isPresent();
        assertThat(row.get().accountNumber()).isEqualTo("00000001");
        assertThat(row.get().reference()).isEqualTo("000000000127");
    }
}
