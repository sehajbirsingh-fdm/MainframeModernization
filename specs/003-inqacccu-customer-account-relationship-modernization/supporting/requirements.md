# requirements.md - INQACCCU Modernization Requirements

## Purpose

Define what the modernized INQACCCU feature must do while preserving confirmed legacy business behavior.

## Scope

These requirements describe business capability and observable behavior only. They do not define implementation structure, technology choices, endpoint design, deployment, or task sequencing.

## Functional Requirements

| ID | Requirement |
|---|---|
| FR-001 | The system shall accept a customer-number inquiry as the business inquiry key. |
| FR-002 | The system shall perform customer validation through an INQCUST-equivalent capability before any account retrieval is attempted. |
| FR-003 | The system shall treat reserved customer numbers 0000000000 and 9999999999 as customer-not-found outcomes, consistent with legacy behavior. |
| FR-004 | The system shall derive sort code internally as fixed value 987654 for account retrieval behavior and shall not require sort code as caller input. |
| FR-005 | The system shall retrieve customer-associated account data in read-only inquiry mode. |
| FR-006 | The system shall preserve customer-found with zero accounts as a valid successful outcome. |
| FR-007 | The system shall preserve a maximum returned account count of 20 per inquiry as baseline behavior unless changed by future approved scope. |
| FR-008 | The system shall preserve normal end-of-data behavior equivalent to SQLCODE +100 as non-error completion. |
| FR-009 | The system shall preserve legacy status semantics at the business boundary, including success indicator, failure indicator, customer-found indicator, and returned-account count. |
| FR-010 | The system shall preserve legacy failure-path distinctions, including customer-validation/customer-not-found and account retrieval failure categories equivalent to cursor open, fetch, and close failures. |
| FR-011 | The system shall preserve complete returned account information: eyecatcher, customer number, sort code, account number, account type, interest rate, opened date, overdraft limit, last statement date, next statement date, available balance, and actual balance. |
| FR-012 | The system shall preserve fixed-width identifier semantics for customer number and account number, including preservation of leading zeroes. |
| FR-013 | The system shall preserve legacy date semantics where account dates originate from DB2 date context and legacy output meaning corresponds to DDMMYYYY representation. |
| FR-014 | The system shall not imply deterministic account ordering unless an explicit future scope decision introduces ordering behavior. |
| FR-015 | The system shall provide a usable user-facing inquiry channel through which a user can initiate a customer account inquiry using customer number. |
| FR-016 | The system shall present associated account results to the user when inquiry outcomes indicate customer found with one or more accounts. |
| FR-017 | The system shall present a distinct user-visible outcome when inquiry results indicate customer found with zero accounts. |
| FR-018 | The system shall present a distinct user-visible outcome when inquiry results indicate customer not found. |
| FR-019 | The system shall provide user-visible feedback for invalid inquiry input consistent with validation failure behavior. |
| FR-020 | The system shall present a distinct user-visible outcome when non-business infrastructure failure occurs. |
| FR-021 | The system shall preserve leading zeroes in externally visible customer and account identifiers throughout inquiry input and output presentation. |
| FR-022 | The system shall allow users to perform subsequent inquiries by modifying inquiry input and submitting another request through the same inquiry interaction. |

## Optional Future Enhancements (Not Mandatory Requirements)

- Future scope may revise the 20-record bound.
- Future scope may introduce deterministic ordering rules.
- Future scope may define alternate external date representations.

None of the above are required by this baseline requirements set.

## Open Questions (Business and Policy Decisions)

1. What external interface style and contract conventions are required by product policy?
2. What authentication and authorization policies are required for this feature?
3. Should future scope intentionally change the legacy 20-record response bound?
4. Should future scope introduce deterministic ordering for returned accounts?
5. What production data-source strategy is required for long-term operation?
6. What external date representation policy is required while preserving legacy business semantics?
