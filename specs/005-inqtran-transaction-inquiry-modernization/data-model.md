# Data Model

## Domain Objects

```java
public record TransactionInquiryQuery(
	String sortCode,
	String accountNumber,
	String fromDate,
	String toDate,
	int limit,
	int offset
) {}

public record AccountTransaction(
	String transactionId,
	String sortCode,
	String accountNumber,
	String date,
	String time,
	String reference,
	String type,
	String description,
	BigDecimal amount
) {}

public record TransactionInquiryResponse(
	String sortCode,
	String accountNumber,
	String fromDate,
	String toDate,
	int limit,
	int offset,
	int totalCount,
	int returnedCount,
	List<AccountTransaction> transactions
) {}
```

Names are illustrative Java target names; field set and types are contractually governed by the mapping matrix and OpenAPI.

## Persistence Model

A target H2 table must map the evidenced `PROCTRAN` columns:

- eyecatcher `CHAR(4)`
- sort code `CHAR(6)` not null
- account number `CHAR(8)` not null
- transaction date `DATE` (nullability unresolved)
- time `CHAR(6)`
- reference `CHAR(12)`
- type `CHAR(3)`
- description `CHAR(40)`
- amount `DECIMAL(12,2)`

Do not create an entity with invented primary key, audit fields, status, category, balance, merchant, currency, or posting metadata.

## Relationships and Cardinality
- One account identity (sort code + account number) may have zero to many `PROCTRAN` rows.
- No foreign key to an account table is evidenced in supplied DDL.
- Composite transaction ID is a representation assembled from five row values; database uniqueness is not evidenced.

## Validation Rules
- Fixed-width identifiers are strings in modern code.
- API width/pattern: 6-digit sort code, 8-digit account number, 8-digit dates, 6-digit time, 12-character reference, 3-character type.
- Amount uses `BigDecimal` with scale two.
- Null handling requires DDL/SME validation before mapper completion.

## Type Conversion
- COMMAREA numeric display identifiers -> Java strings.
- DB2 DATE -> API `YYYYMMDD` string.
- DB2 packed decimal/DECIMAL(12,2) -> `BigDecimal`.
- Effective limit/offset -> Java integers.
- DB2 fixed CHAR padding behavior -> unresolved; do not silently normalize identifiers.
