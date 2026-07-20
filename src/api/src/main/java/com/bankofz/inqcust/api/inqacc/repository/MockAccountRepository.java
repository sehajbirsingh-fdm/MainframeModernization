package com.bankofz.inqcust.api.inqacc.repository;

import com.bankofz.inqcust.api.inqacc.domain.AccountRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "app.data.mode", havingValue = "mock", matchIfMissing = true)
public class MockAccountRepository implements AccountRepository {

    private final List<AccountRecord> accounts;

    public MockAccountRepository(
            @Value("${app.inqacc.mock-data.path:mock-data/account-records.json}") String mockDataPath,
            ObjectMapper objectMapper
    ) {
        this.accounts = loadAccounts(mockDataPath, objectMapper);
    }

    @Override
    public Optional<AccountRecord> findBySortcodeAndAccountNumber(String sortcode, String accountNumber) {
        return accounts.stream()
                .filter(account -> sortcode.equals(account.sortcode())
                        && accountNumber.equals(account.accountNumber()))
                .findFirst();
    }

    @Override
    public Optional<AccountRecord> findHighestAccountNumberBySortcode(String sortcode) {
        return accounts.stream()
                .filter(account -> sortcode.equals(account.sortcode()))
                // Account numbers are fixed-width 8-digit strings; lexicographic order matches numeric order.
                .max((left, right) -> left.accountNumber().compareTo(right.accountNumber()));
    }

    private List<AccountRecord> loadAccounts(String mockDataPath, ObjectMapper objectMapper) {
        try (InputStream inputStream = openInputStream(mockDataPath)) {
            MockDataFile dataFile = objectMapper.readValue(inputStream, MockDataFile.class);
            if (dataFile == null || dataFile.accounts() == null) {
                return Collections.emptyList();
            }
            return dataFile.accounts();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load mock account data", exception);
        }
    }

    private InputStream openInputStream(String mockDataPath) throws IOException {
        String resolvedPath = mockDataPath == null ? "" : mockDataPath;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MockDataFile(List<AccountRecord> accounts) {
    }
}
