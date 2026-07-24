package com.bankofz.inqcust.api.inqacccu.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAccountRelationshipRepositoryTest {

    @Test
  void shouldLoadRelationshipByCustomerNumber() throws Exception {
        String payload = """
                [
                  {
                    \"customer\": {
                      \"customerNumber\": \"0000000001\",
                      \"customerName\": \"John Smith\",
                      \"sortCode\": \"123456\",
                      \"customerType\": \"INDIVIDUAL\"
                    },
                    \"accounts\": [
                      {
                        \"accountNumber\": \"1000000001\",
                        \"sortCode\": \"123456\",
                        "openedDate": 20200115,
                        \"accountType\": \"CHK\",
                        \"accountTypeDescription\": \"Checking Account\",
                        \"availableBalance\": 1520.45,
                        \"actualBalance\": 1498.12,
                        \"interestRate\": 0.5,
                        \"overdraftLimit\": 500,
                        \"lastStatementDate\": 20251231,
                        \"nextStatementDate\": 20260131
                      }
                    ]
                  }
                ]
                """;

              Path tempFile = Files.createTempFile("inqacccu-relationships", ".json");
              Files.writeString(tempFile, payload, StandardCharsets.UTF_8);

        JsonAccountRelationshipRepository repository = new JsonAccountRelationshipRepository(
                tempFile.toString(),
                new ObjectMapper()
        );

        var result = repository.findByCustomerNumber("0000000001");

        assertThat(result).isPresent();
        assertThat(result.get().accounts()).hasSize(1);
        assertThat(result.get().accounts().get(0).accountNumber()).isEqualTo("1000000001");
    }
}
