# Prompt 03 - Repository and mock data

Implement the repository layer using `docs/spec.md`, `docs/architecture.md`, and `mock-data/customer-records.json`.

Create:
- CustomerRepository interface
- MockCustomerRepository implementation

Repository interface must include:
```java
Optional<CustomerRecord> findBySortCodeAndCustomerNumber(String sortCode, String customerNumber);
Optional<CustomerRecord> findLatestBySortCode(String sortCode);
List<CustomerRecord> findBySortCode(String sortCode);
```

MockCustomerRepository must:
- Load mock data from classpath resource `mock-data/customer-records.json`.
- Trim fixed-width strings when mapping if needed.
- Sort customer numbers numerically or lexicographically safely because they are zero-padded 10 digit strings.
- Return highest customer number for latest lookup.

Do not use a real database.
Do not use JPA.
