package com.bankofz.inqcust.api.inqacccu.repository;

import com.bankofz.inqcust.api.inqacccu.exception.RepositoryUnavailableException;
import com.bankofz.inqcust.api.inqacccu.repository.model.AccountProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.CustomerProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonAccountRelationshipRepository implements AccountRelationshipRepository {

        private final String mockDataPath;
        private final ObjectMapper objectMapper;

        public JsonAccountRelationshipRepository(
                        @Value("${app.inqacccu.mock-data.path:mock-data/account-relationship-records.json}") String mockDataPath,
                        ObjectMapper objectMapper
        ) {
                this.mockDataPath = mockDataPath;
                this.objectMapper = objectMapper;
        }

    @Override
    public Optional<RelationshipProjection> findByCustomerNumber(String customerNumber) {
        List<RelationshipRecord> allRecords = readAll();

        return allRecords.stream()
                .filter(record -> customerNumber.equals(record.customer().customerNumber()))
                .findFirst()
                .map(this::toProjection);
    }

    private List<RelationshipRecord> readAll() {
                try (InputStream inputStream = openInputStream(mockDataPath)) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new RepositoryUnavailableException("Unable to read account relationship mock data", exception);
        }
    }

        private InputStream openInputStream(String pathValue) throws IOException {
                String resolvedPath = pathValue == null ? "" : pathValue;

                Path directPath = Path.of(resolvedPath);
                if (Files.exists(directPath)) {
                        return Files.newInputStream(directPath);
                }

                Path repoRelativePath = Path.of("..", "..", resolvedPath);
                if (Files.exists(repoRelativePath)) {
                        return Files.newInputStream(repoRelativePath);
                }

                ClassPathResource resource = new ClassPathResource(resolvedPath);
                if (resource.exists()) {
                        return resource.getInputStream();
                }

                throw new IOException("Mock data file not found: " + resolvedPath);
        }

    private RelationshipProjection toProjection(RelationshipRecord record) {
        CustomerProjection customer = new CustomerProjection(
                record.customer().customerNumber(),
                record.customer().customerName(),
                record.customer().sortCode(),
                record.customer().customerType()
        );

        List<AccountProjection> accounts = record.accounts().stream()
                .map(account -> new AccountProjection(
                        account.accountNumber(),
                        account.sortCode(),
                        account.accountType(),
                        account.openedDate(),
                        account.availableBalance(),
                        account.actualBalance(),
                        account.interestRate(),
                        account.overdraftLimit(),
                        account.lastStatementDate(),
                        account.nextStatementDate()
                ))
                .toList();

        return new RelationshipProjection(customer, accounts);
    }

    private record RelationshipRecord(
            CustomerRecord customer,
            List<AccountRecord> accounts
    ) {
    }

    private record CustomerRecord(
            String customerNumber,
            String customerName,
            String sortCode,
            String customerType
    ) {
    }

    private record AccountRecord(
            String accountNumber,
            String sortCode,
            String accountType,
            String accountTypeDescription,
            Integer openedDate,
            java.math.BigDecimal availableBalance,
            java.math.BigDecimal actualBalance,
            java.math.BigDecimal interestRate,
            Integer overdraftLimit,
            Integer lastStatementDate,
            Integer nextStatementDate
    ) {
    }
}
