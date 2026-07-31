# Business Rules — INQTRANL

This artifact defines observable legacy business behavior for INQTRANL. It intentionally excludes modernization design, interface protocol design, and implementation recommendations.

Evidence labels used below:
- **Confirmed Evidence:** directly visible in legacy source/copybooks.
- **Reasonable Inference:** strongly suggested by legacy behavior but not explicitly guaranteed.
- **Remaining Uncertainty:** not provable from supplied artifacts.

## BR-001 — Account identity filters transactions
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` SQL declarations `TRAN-CURSOR` and `TRAN-COUNT-CURSOR` filter on `PROCTRAN_SORTCODE = :HV-QUERY-SORTCODE` and `PROCTRAN_NUMBER = :HV-QUERY-ACCNO`.
- **Business rule:** Only transactions for the exact requested sort code and account number are considered.

## BR-002 — Date range filtering is inclusive
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` SQL declarations apply `PROCTRAN_DATE >= :HV-QUERY-FROM-DATE` and `PROCTRAN_DATE <= :HV-QUERY-TO-DATE`.
- **Business rule:** Transactions on supplied start and end boundaries are included.

## BR-003 — No from-date is normalized to the legacy minimum sentinel
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cpy` defines `INQTRANL-NO-FROM-DATE VALUE 0`; `INQTRANL.cbl` paragraph `A010` sets `INQTRANL-FROM-DATE` to `0`; `GTC010`/`RTD010` convert to ISO text before SQL use.
- **Business rule:** A missing from-date is normalized through the legacy minimum-sentinel conversion path before SQL predicate evaluation.
- **Remaining Uncertainty:** The runtime query effect of the converted sentinel value is not provable from supplied artifacts alone.

## BR-004 — No to-date is normalized to the legacy maximum sentinel
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cpy` defines `INQTRANL-NO-TO-DATE VALUE 99999999`; `INQTRANL.cbl` paragraph `A010` sets `INQTRANL-TO-DATE` to `99999999`; `GTC010`/`RTD010` convert to ISO text before SQL use.
- **Business rule:** A missing to-date is normalized through the legacy maximum-sentinel conversion path before SQL predicate evaluation.
- **Remaining Uncertainty:** The runtime query effect of the converted sentinel value is not provable from supplied artifacts alone.

## BR-005 — Limit value 0 is normalized to 50
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` paragraph `A010`: `IF INQTRANL-LIMIT = 0 MOVE 50 TO INQTRANL-LIMIT`.
- **Business rule:** If incoming limit is 0, the effective limit becomes 50.

## BR-006 — Limit values above 100 are normalized to 100
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` paragraph `A010`: `IF INQTRANL-LIMIT > 100 MOVE 100 TO INQTRANL-LIMIT`.
- **Business rule:** Incoming limits above 100 are capped to 100.

## BR-007 — Maximum returned rows per inquiry is 100
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cpy` declares `INQTRANL-TRANSACTIONS OCCURS 100`; `INQTRANL.cbl` caps limit to 100 in `A010`.
- **Business rule:** A single successful inquiry returns no more than 100 transaction rows.

## BR-008 — Offset is applied after filtering/ordering and before row return
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` paragraph `FTD010` fetches ordered rows, increments `WS-SKIP-COUNT` while `< HV-QUERY-OFFSET`, and only then increments `WS-FETCH-COUNT` and populates transaction output rows.
- **Business rule:** Row skipping occurs first; returned rows begin only after offset rows have been skipped.

## BR-009 — Ordering is descending date, then descending time
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `TRAN-CURSOR`: `ORDER BY PROCTRAN_DATE DESC, PROCTRAN_TIME DESC`.
- **Business rule:** Returned rows are ordered newest date first, then newest time first.
- **Remaining Uncertainty:** For rows sharing identical date and time, no third tie-breaker is declared.

## BR-010 — Total count is pre-pagination
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `TRAN-COUNT-CURSOR` is `SELECT COUNT(*)` with account/date filters only; `FTD010` applies offset/limit in the separate list path.
- **Business rule:** Total count reflects all matching rows after filters, not the offset/limit subset.

## BR-011 — Returned count equals rows actually populated in the output array
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `FTD010` increments `WS-FETCH-COUNT` only when a row is accepted after offset; `A010` moves `WS-FETCH-COUNT` to `INQTRANL-RETURNED-COUNT`.
- **Business rule:** Returned count is the number of transaction rows actually placed into the output array.

## BR-012 — Empty result is a successful inquiry outcome
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `FTD010` treats `SQLCODE = 100` as normal end-of-data; `A010` then sets success to `Y` and returns counts.
- **Business rule:** No matching transactions is a successful outcome with zero returned rows.

## BR-013 — Success flag is set on normal completion
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `A010` sets `INQTRANL-SUCCESS` to `Y` after total-count and list processing, then returns.
- **Business rule:** On the normal path, success is marked true.

## BR-014 — Invalid eyecatcher is normalized, not rejected
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cpy` defines valid eyecatcher `ITRL`; `INQTRANL.cbl` `A010`: `IF NOT INQTRANL-EYE-VALID MOVE 'ITRL' TO INQTRANL-EYE`.
- **Business rule:** If eyecatcher is invalid, it is overwritten with `ITRL` and processing continues.

## BR-015 — Composite transaction identifier is deterministic in shape
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `WS-TRAN-ID-PARTS` and `FTD010` compose sortcode + `-` + account + `-` + date + `-` + time + `-` + reference; moved to `INQTRANL-TRAN-ID`.
- **Business rule:** Each returned row includes a five-part hyphen-delimited transaction identifier in that exact component order.

## BR-016 — Output transaction fields are direct row values, with date reformatted for output
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` `FTD010` moves fetched sort code, account, time, reference, type, description, and amount directly into output fields; date is converted from ISO text to `YYYYMMDD` by `CONVERT-ISO-TO-YYYYMMDD` before output move.
- **Business rule:** Returned transaction data mirrors selected row values, except date format conversion to the 8-digit output representation.
- **Remaining Uncertainty:** Character-padding behavior for fixed-width character columns is not explicitly normalized in the shown logic.

## BR-017 — Count and list paths use the same filter criteria
- **Classification:** Confirmed Evidence
- **Legacy evidence:** both `TRAN-COUNT-CURSOR` and `TRAN-CURSOR` in `INQTRANL.cbl` use the same sort code, account number, and inclusive date predicates.
- **Business rule:** The population counted by total count and the population eligible for row return are based on the same filter set.

## BR-018 — Technical SQL failures do not produce partial successful output
- **Classification:** Confirmed Evidence
- **Legacy evidence:** `INQTRANL.cbl` paragraphs `GTC010`, `RTD010`, `FTD010` route non-acceptable SQLCODE paths to `ABEND-ROUTINE`; `AR010` links `ABNDPROC` and executes `EXEC CICS ABEND ABCODE('ITRL')`; normal success assignment in `A010` occurs only after successful processing completes.
- **Business rule:** On technical SQL failure, processing terminates via abend path instead of returning a partially successful inquiry response.

## BR-019 — Composite identifier components align with detail-inquiry key fields
- **Classification:** Reasonable Inference
- **Legacy evidence:** `INQTRANL` composite ID uses sort code, account, date, time, reference; `INQTRAND.cbl` lookup key uses the same five fields in `TRAND-CURSOR` predicates.
- **Business rule:** The list identifier structure is suitable for correlating to the detail inquiry key components.
