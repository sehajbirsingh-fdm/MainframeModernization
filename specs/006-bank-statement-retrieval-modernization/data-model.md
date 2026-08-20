# Data Model - 006 Bank Statement Retrieval

## Core Concepts
- AccountIdentity
  - sortCode
  - accountNumber
- StatementPeriod
  - period (YYYYMM)
- StatementSummary
  - periodFrom (YYYYMMDD)
  - periodTo (YYYYMMDD)
  - openingBalance
  - totalCredits
  - totalDebits
  - closingBalance
  - transactionCount
- StatementEntry
  - date
  - time
  - reference
  - type
  - description
  - amount
- AccountStatement
  - accountIdentity
  - statementPeriod
  - summary
  - entries[]

## Invariants
- Read-only operation.
- Statement entries constrained to selected period.
- Summary totals must align with returned entries and period rules.
