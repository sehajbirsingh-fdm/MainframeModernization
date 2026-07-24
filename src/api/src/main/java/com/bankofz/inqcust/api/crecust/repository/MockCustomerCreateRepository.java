package com.bankofz.inqcust.api.crecust.repository;

import com.bankofz.inqcust.api.domain.CustomerRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MockCustomerCreateRepository implements CustomerCreateRepository {

    private final List<CustomerRecord> customers;
    private final Map<String, Long> latestBySortCode;

    public MockCustomerCreateRepository(
            @Value("${app.mock-data.path:mock-data/customer-records.json}") String mockDataPath,
            ObjectMapper objectMapper
    ) {
        this.customers = new ArrayList<>(loadCustomers(mockDataPath, objectMapper));
        this.latestBySortCode = new ConcurrentHashMap<>();

        for (CustomerRecord customer : customers) {
            long number = parseCustomerNumber(customer.customerNumber());
            latestBySortCode.merge(customer.sortCode(), number, Math::max);
        }
    }

    @Override
    public synchronized long nextCustomerNumber(String sortCode) {
        long next = latestBySortCode.getOrDefault(sortCode, 0L) + 1;
        latestBySortCode.put(sortCode, next);
        return next;
    }

    @Override
    public synchronized CustomerRecord save(CustomerRecord customerRecord) {
        customers.add(customerRecord);
        return customerRecord;
    }

    private long parseCustomerNumber(String customerNumber) {
        try {
            return Long.parseLong(customerNumber.trim());
        } catch (Exception exception) {
            return 0L;
        }
    }

    private List<CustomerRecord> loadCustomers(String mockDataPath, ObjectMapper objectMapper) {
        try (InputStream inputStream = openInputStream(mockDataPath)) {
            MockDataFile dataFile = objectMapper.readValue(inputStream, MockDataFile.class);
            if (dataFile == null || dataFile.customers() == null) {
                return List.of();
            }
            return dataFile.customers();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load mock customer data", exception);
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
    private record MockDataFile(List<CustomerRecord> customers) {
    }
}
