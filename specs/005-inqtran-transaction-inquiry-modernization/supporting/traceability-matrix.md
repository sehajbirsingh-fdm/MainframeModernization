# Traceability Matrix - Feature 005 INQTRAN Transaction Inquiry Modernization

## 1. Purpose
This matrix provides end-to-end traceability across legacy evidence -> business rules -> requirements -> specification -> OpenAPI -> implementation plan -> tasks -> test cases -> review and verification evidence.

This matrix is a linkage artifact only. It does not define new behavior.

## 2. Scope
Included traceability coverage:
- Exact account filtering.
- Supplied date filtering.
- Optional omitted-date modernization behavior.
- Limit and offset handling.
- Ordering.
- Count/list parity.
- totalCount and returnedCount semantics.
- Empty-success behavior.
- Mapping and composite transaction identifier behavior.
- Read-only behavior.
- Technical-failure behavior and no-partial-success invariant.
- Frontend request and state behavior.
- Security and safe error exposure.
- Logging and observability.
- Repository compatibility and no-parallel-structure boundaries.
- Runtime OpenAPI reconciliation.
- Testing and regression.
- Configuration and operational boundaries.
- Documentation, QA, code review, and demo readiness.

Excluded:
- INQTRAND transaction-detail behavior.
- Live production DB2 or CICS connectivity.
- New feature-specific authorization behavior.
- Arbitrary coverage or performance targets.
- Unsupported future behavior.

## 3. Traceability Principles
- Every row is grounded in an authoritative source.
- Every requirement maps to implementation and verification or is explicitly marked as gap/deferred.
- Business rules remain distinct from approved modernization decisions.
- OpenAPI trace is transport-contract trace only.
- Plan trace references implementation-approach sections.
- Task trace references executable T### work items.
- Test trace references finalized TC-### IDs.
- Exact section headings are used where stable IDs do not exist.
- Unresolved evidence remains visible and is not silently converted to approved behavior.

## 4. Artifact Responsibilities
| Artifact | Traceability responsibility |
|---|---|
| program-analysis.md | legacy program evidence |
| dependency-map.md | legacy dependency evidence |
| business-rules.md | verified and approved behavioral rules |
| mapping-matrix.md | field and transformation mapping |
| intended-system.md | target capability boundaries |
| architecture.md | component and responsibility boundaries |
| research.md | technical decisions and unresolved evidence |
| data-model.md | conceptual structures and invariants |
| supporting/requirements.md | implementation obligations |
| spec.md | approved system and user-visible behavior |
| contracts/openapi.yaml | transport contract |
| plan.md | implementation approach |
| tasks.md | executable implementation work |
| supporting/test-spec.md | verification scenarios |
| checklists/requirements.md | requirements quality verification |
| traceability-matrix.md | complete cross-artifact linkage |

Execution status note:
- Implementation, tests, QA, and review are planned and traced; execution evidence is pending.

### Core Traceability Matrix
| Trace ID | Capability or behavior | Evidence classification | Legacy/source evidence | Business rule | Requirement(s) | Specification trace | OpenAPI trace | Plan trace | Task trace | Test trace | Status / notes |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TR-001 | Exact sort code and account number filtering | Verified legacy behavior | program-analysis.md -> SQL Analysis -> predicates on sortcode/account | BR-001 | FR-001 | US-001, AC-001 | GET path params sortCode/accountNumber | Backend Component Design -> Repository Design | T020, T021, T054 | TC-001, TC-039 | Planned and traced |
| TR-002 | Leading-zero identifier preservation | Non-functional requirement | mapping-matrix.md -> Core Field and Flow Mapping sortCode/accountNumber/reference | BR-016 | NFR-002, FR-001, FR-009 | Feature Invariants leading-zero statement | path param/string schemas and Transaction string fields | Mapper Design, Validation Strategy | T023, T051, T059 | TC-029, TC-051, TC-052, TC-071 | Planned and traced |
| TR-003 | Inclusive supplied fromDate | Verified legacy behavior | program-analysis.md -> SQL predicates >= from | BR-002 | FR-002 | AC-002 | query param fromDate pattern | Persistence Strategy | T028, T054 | TC-002 | Planned and traced |
| TR-004 | Inclusive supplied toDate | Verified legacy behavior | program-analysis.md -> SQL predicates <= to | BR-002 | FR-002 | AC-002 | query param toDate pattern | Persistence Strategy | T028, T054 | TC-003 | Planned and traced |
| TR-005 | Only fromDate supplied | Approved modernization behavior | supporting/requirements.md -> Modernization Enhancements Requiring Approval (optional boundary contract decision) | BR-003, BR-004 | FR-002, FR-013 | AC-002, AC-014, and Omitted-date processing note | query parameters fromDate and toDate optional | Service Design, Legacy-to-Modern Transformation Strategy | T028, T049, T063, T067, T073 | TC-004 | Planned and traced; contract verification only |
| TR-006 | Only toDate supplied | Approved modernization behavior | supporting/requirements.md modernization decision source; research.md Decision: Legacy Sentinel Handling | BR-003, BR-004 | FR-002, FR-013 | AC-002, AC-014, and Omitted-date processing note | query parameters fromDate and toDate optional | Service Design, Legacy-to-Modern Transformation Strategy | T028, T049, T063, T067, T073 | TC-005 | Planned and traced; contract verification only |
| TR-007 | Both dates omitted | Approved modernization behavior | supporting/requirements.md modernization decision source | BR-003, BR-004 | FR-002, FR-013 | AC-002, AC-014, and Error Handling omitted-date caveat | query parameters fromDate and toDate optional | Plan Overview date-handling distinction | T028, T049, T063, T067, T073 | TC-006, TC-086 | Planned and traced; contract verification only |
| TR-008 | Legacy omitted-date sentinel evidence limitation | Verified legacy behavior | program-analysis.md -> SQL Analysis omitted-date uncertainty and always-present predicates | BR-003, BR-004 | FR-002, FR-013 | Omitted-date processing note and Edge Cases | fromDate/toDate descriptions explicitly reject asserted legacy unconstrained truth | Plan Overview and Risks and Mitigations unresolved sentinel runtime behavior | T073 | TC-041 (parity), TC-086 (modern behavior only) | Planned and traced; legacy equivalence unresolved |
| TR-009 | Default limit | Verified legacy behavior | program-analysis.md -> input normalization IF limit=0 MOVE 50 | BR-005 | FR-003 | AC-003 | limit default 50 description | Service Design | T026, T049 | TC-007 | Planned and traced |
| TR-010 | Zero-limit normalization | Verified legacy behavior | program-analysis.md -> input normalization limit 0 -> 50 | BR-005 | FR-003 | AC-003 | limit schema minimum/default and description | Validation Strategy | T026, T049 | TC-008, TC-035 | Planned and traced |
| TR-011 | Maximum-limit clamping | Verified legacy behavior | business-rules.md BR-006 and BR-007 | BR-006, BR-007 | FR-003 | AC-003 | limit behavior description; TransactionListResponse max rows | Service Design | T027, T049, T055 | TC-009, TC-010, TC-036 | Planned and traced |
| TR-012 | Non-negative offset | Functional validation requirement | supporting/requirements.md SR-002 and spec validation rules | Not applicable | FR-004, SR-002 | Validation Rules | query parameter offset minimum 0 | Validation Strategy | T025, T033, T047, T057 | TC-033, TC-034 | Planned and traced |
| TR-013 | Offset within results | Verified legacy behavior | program-analysis.md -> pagination loop skips then accepts rows | BR-008 | FR-004 | AC-004 | offset parameter semantics | Service Design | T027, T055 | TC-012 | Planned and traced |
| TR-014 | Offset equal to total | Verified legacy behavior | business-rules.md BR-012 plus AC-008 behavior | BR-012 | FR-006, FR-007 | AC-008 | 200 emptyOffsetBeyondTotal example | Error-Handling Strategy | T030, T057 | TC-013 | Planned and traced |
| TR-015 | Offset greater than total | Verified legacy behavior | program-analysis.md -> SQLCODE 100 loop termination with zero accepted rows possible | BR-012 | FR-004, FR-007 | AC-008 and Edge Cases | 200 response semantics for empty page | Service Design | T030, T055, T064 | TC-014, TC-015, TC-089 | Planned and traced |
| TR-016 | Ordering by date descending | Verified legacy behavior | program-analysis.md -> ORDER BY date DESC | BR-009 | FR-005 | AC-005 | transactions ordering statement | Repository Design | T021, T055 | TC-042 | Planned and traced |
| TR-017 | Ordering by time descending within date | Verified legacy behavior | program-analysis.md -> ORDER BY time DESC secondary key | BR-009 | FR-005 | AC-005 | transactions ordering statement | Repository Design | T021, T055 | TC-043 | Planned and traced |
| TR-018 | No guaranteed ordering for date/time ties | Compatibility requirement | program-analysis.md -> no third tie-breaker evidence | BR-009 | FR-005 | AC-005 tie note; Edge Cases | transactions description no additional tie-break ordering | Legacy-to-Modern Transformation Strategy ordering precision statement | T055, T078 | TC-044 | Planned and traced |
| TR-019 | Ordering before pagination | Verified legacy behavior | program-analysis.md -> SQL ordering plus loop offset/limit behavior | BR-008, BR-009 | FR-004, FR-005 | AC-004, AC-005 | offset description and ordered transactions note | Service Design and Repository Design | T021, T027, T055 | TC-045, TC-046 | Planned and traced |
| TR-020 | Filtered totalCount | Verified legacy behavior | program-analysis.md -> separate COUNT cursor pre-pagination | BR-010 | FR-006 | AC-006 | TransactionListResponse.totalCount | Repository Design | T019, T020, T029, T050 | TC-021 | Planned and traced |
| TR-021 | Page returnedCount | Verified legacy behavior | program-analysis.md -> WS-FETCH-COUNT moved to returned count | BR-011 | FR-006 | AC-006 | TransactionListResponse.returnedCount | Service Design | T017, T029, T050 | TC-022 | Planned and traced |
| TR-022 | Count/list filter parity | Verified legacy behavior | program-analysis.md and business-rules.md same filters both paths | BR-017 | FR-006 | AC-006 | 200 schema counts with consistent filter semantics | Repository Design | T022, T054 | TC-039, TC-040, TC-041 | Planned and traced |
| TR-023 | Empty no-match success | Verified legacy behavior | business-rules.md BR-012, BR-013 | BR-012, BR-013 | FR-007 | AC-007 | 200 emptyNoMatch example | Error-Handling Strategy | T030, T034, T057 | TC-016, TC-087 | Planned and traced |
| TR-024 | Offset-beyond-total empty success | Verified legacy behavior | spec.md AC-008 and program-analysis offset loop semantics | BR-012 | FR-006, FR-007 | AC-008 | 200 emptyOffsetBeyondTotal example | Error-Handling Strategy | T030, T041, T064 | TC-013, TC-014, TC-089 | Planned and traced |
| TR-025 | Read-only persistence behavior | Verified legacy behavior | supporting/program-analysis.md -> SQL Analysis -> TRAN-CURSOR FOR FETCH ONLY and no write SQL | Not applicable - traced directly to supporting/program-analysis.md read-only SQL evidence and FR-010 | FR-010 | AC-011 and Feature Invariants | GET operation description read-only inquiry | Persistence Strategy | T020, T021, T056, T078 | TC-018, TC-048 | Planned and traced |
| TR-026 | Composite transaction identifier | Verified legacy behavior | mapping-matrix.md transaction ID composition path | BR-015, BR-019 | FR-008 | AC-009 | Transaction.transactionId | Mapper Design | T023, T051 | TC-058 | Planned and traced |
| TR-027 | Transaction field mapping | Verified legacy behavior | mapping-matrix.md core mapping rows | BR-016 | FR-009 | AC-010 | Transaction schema field set | Mapper Design | T017, T023, T051 | TC-051, TC-052, TC-056 | Planned and traced |
| TR-028 | Date/time/reference mapping | Verified legacy behavior | mapping-matrix.md date/time/ref mapping rows | BR-016 | FR-009 | AC-010 | Transaction.date/time/reference schemas | Mapper Design | T023, T051 | TC-053, TC-054, TC-055 | Planned and traced |
| TR-029 | Positive/negative/decimal amount mapping | Verified legacy behavior | mapping-matrix.md amount row DECIMAL semantics | BR-016 | FR-009 | AC-010 | Transaction.amount format and multipleOf | Mapper Design | T023, T051 | TC-057 | Planned and traced |
| TR-030 | Count-stage technical failure | Verified legacy behavior | program-analysis.md -> OPEN/FETCH/CLOSE count errors route to abend | BR-018 | FR-011 | AC-012 | 500 response | Error-Handling Strategy | T024, T031, T035, T056, T057 | TC-019, TC-049 | Planned and traced |
| TR-031 | Row-stage technical failure | Verified legacy behavior | program-analysis.md -> list fetch errors route to abend | BR-018 | FR-011 | AC-012 | 500 response | Error-Handling Strategy | T024, T031, T035, T056, T057 | TC-020, TC-050 | Planned and traced |
| TR-032 | No-partial-success invariant | Verified legacy behavior | business-rules.md BR-018 | BR-018 | FR-011 | Feature Invariants and AC-012 | 500 response semantics | Service Design and Error-Handling Strategy | T031, T052, T065 | TC-091 | Planned and traced |
| TR-033 | Structural validation | Approved validation behavior | supporting/requirements.md SR-002 | Not applicable | SR-002, NFR-006 | Validation Rules | path/query patterns and minimum constraints | Validation Strategy | T025, T033, T057, T061, T065 | TC-023 through TC-034, TC-090 | Planned and traced |
| TR-034 | Safe error response | Security requirement | supporting/requirements.md NFR-008 and SR-004 | Not applicable | NFR-008, SR-004 | Error Handling | ErrorResponse schema | Error-Handling Strategy, Security Strategy | T018, T035, T042, T053, T061 | TC-096, TC-097 | Planned and traced |
| TR-035 | HTTP 200, 400, and 500 behavior | Approved modernization behavior | spec.md Error Handling and AC set | BR-012, BR-018 where relevant | FR-007, FR-011, SR-002 | AC-007, AC-012, Validation Rules | responses 200, 400, 500 | Controller Design and Error-Handling Strategy | T034, T035, T048, T057 | TC-064, TC-065, TC-066, TC-067 | Planned and traced |
| TR-036 | No 404 for empty transaction results | Approved modernization behavior | spec.md Error Handling no 404 | BR-012 context | FR-007 | AC-007, Error Handling | 500 description notes no 404 for empty results | Error-Handling Strategy | T034, T048, T057 | TC-068 | Planned and traced |
| TR-037 | Runtime OpenAPI reconciliation | Operational requirement | supporting/requirements.md OR-005 | Not applicable | OR-005 | Document-level traceability and contract notes | runtime openapi publication reconciliation target | API Contract Strategy | T045, T046, T058, T070 | TC-069 | Planned and traced |
| TR-038 | Frontend form and request composition | Approved modernization behavior | intended-system.md frontend responsibilities | Not applicable | FR-012 | AC-013 | endpoint path and parameter contract | Frontend Design | T038, T039, T059 | TC-071, TC-072, TC-073 | Planned and traced |
| TR-039 | Optional query omission | Approved modernization behavior | openapi.yaml optional query parameters | Not applicable | FR-002, FR-012 | AC-013 and omitted-date notes | optional fromDate/toDate | Frontend Design and Validation Strategy | T039, T047, T059 | TC-074 | Planned and traced |
| TR-040 | Loading state | Approved modernization behavior | intended-system.md frontend responsibilities | Not applicable | FR-012 | AC-013 | Not applicable | Frontend Design | T040, T060 | TC-075 | Planned and traced |
| TR-041 | Populated result state | Approved modernization behavior | intended-system.md flow and result rendering responsibility | Not applicable | FR-012, FR-006, FR-009 | AC-013 | 200 populated schema | Frontend Design | T040, T060 | TC-076, TC-084 | Planned and traced |
| TR-042 | Empty state | Approved modernization behavior | intended-system.md intended UX empty successful result | BR-012 context | FR-007, FR-012 | AC-007, AC-013 | 200 empty examples | Frontend Design | T041, T060, T064 | TC-077, TC-087, TC-089 | Planned and traced |
| TR-043 | Validation-error state | Approved modernization behavior | spec.md Validation Rules and Error Handling | Not applicable | SR-002, FR-012 | AC-013, Validation Rules | 400 response | Frontend Design and Error-Handling Strategy | T042, T061, T065 | TC-078, TC-090 | Planned and traced |
| TR-044 | Technical-error state | Approved modernization behavior | spec.md Error Handling | BR-018 context | FR-011, FR-012 | AC-012, AC-013 | 500 response and ErrorResponse | Frontend Design and Error-Handling Strategy | T042, T061, T065 | TC-079, TC-091 | Planned and traced |
| TR-045 | Pagination UI behavior | Approved modernization behavior | intended-system.md flow + requirements FR-003/4/6 | BR-008, BR-010, BR-011 | FR-003, FR-004, FR-006, FR-012 | AC-003, AC-004, AC-006, AC-013 | limit/offset query parameters and counts schema | Frontend Design | T041, T062, T064 | TC-081, TC-088 | Planned and traced |
| TR-046 | Subsequent inquiry replacement behavior | Approved modernization behavior | intended-system.md frontend responsibility and flow | Not applicable | FR-012, FR-014 | AC-013, AC-015 | Not applicable | Frontend Design | T043, T062, T066 | TC-082, TC-092 | Planned and traced |
| TR-047 | Existing route and shell preservation | Compatibility requirement | architecture.md scope boundaries and deployment view | Not applicable | FR-014, OR-002, OR-009 | AC-015 | Not applicable | Repository Integration Strategy and Frontend Design | T044, T062, T066, T076, T077 | TC-083, TC-093 | Planned and traced |
| TR-048 | Existing security-policy alignment | Security requirement | supporting/requirements.md SR-001 | Not applicable | SR-001, OR-009 | Error Handling and Non-goals | Not applicable | Security Strategy | T078, T079 | TC-094, TC-095 | Planned and traced |
| TR-049 | Logging data minimization | Security requirement | supporting/requirements.md NFR-007, SR-004 | Not applicable | NFR-007, SR-004 | Error Handling technical-failure safety | Not applicable | Logging and Observability | T053, T078 | TC-099 | Planned and traced |
| TR-050 | Correlation/request identification where supported | Operational requirement | supporting/requirements.md NFR-009 and OR-008 | Not applicable | NFR-009, OR-008 | Error Handling correlation note | ErrorResponse.correlationId | Logging and Observability | T053, T078 | TC-098, TC-100 | Planned and traced |
| TR-051 | H2 proof-of-concept persistence | Operational requirement | research.md Decision: H2 for local development | Not applicable | OR-003, NFR-005 | Non-goals exclude live mainframe dependencies | Not applicable | Persistence Strategy, Configuration and Deployment Strategy | T011, T012, T014, T081 | Repository integration evidence: TC-048 through TC-050. No direct standalone TC for initialization/startup compatibility. | Planned and traced |
| TR-052 | No mock JSON persistence | Compatibility requirement | supporting/requirements.md scope and out-of-scope boundaries | Not applicable | OR-003, NFR-005 | Non-goals and Scope | Not applicable | Repository Integration Strategy | T014 | No direct standalone TC; verified by implementation and review evidence | Planned and traced |
| TR-053 | No live DB2/CICS dependency | Operational requirement | supporting/requirements.md OR-004 | Not applicable | OR-004 | Non-goals | Not applicable | Technical Context and Configuration and Deployment Strategy | T082 | No direct standalone TC; operational verification evidence | Planned and traced |
| TR-054 | Future adapter seam | Unresolved production-adapter concern | architecture.md section 15 Extensibility; plan.md Future Mainframe Adapter Options | Not applicable | NFR-005, OR-004 | Non-goals and bounded modernization intent | Not applicable | Adapter Design and Future Mainframe Adapter Options | T014, T019, T082 | Not applicable to current TC set | Covered with deferred production-adapter concern |
| TR-055 | Regression verification | Compatibility requirement | architecture.md section 17 Testing Architecture and section 19 scope boundaries | Not applicable | FR-014, OR-009, NFR-011 | AC-015 | Not applicable | Testing Strategy -> Regression Tests | T058, T076 | TC-083, TC-093 | Planned and traced |
| TR-056 | Documentation and quickstart | Operational requirement | supporting/requirements.md OR-001, OR-002, OR-006 | Not applicable | OR-001, OR-002, OR-006 | Document-level traceability and example request usage | Not applicable | Implementation Phases phase 8 | T070, T071, T072, T073 | No direct standalone TC; documentation evidence | Planned and traced |
| TR-057 | QA and code review | Operational requirement | checklists/requirements.md quality checklist criteria | Not applicable | NFR-010, NFR-011, OR-009 | AC coverage and non-goal checks | Not applicable | Implementation Phases phase 8 | T074, T075, T076, T077, T078, T079 | QA/review evidence mapped to TC groups | Planned and traced |
| TR-058 | Demo readiness | Operational requirement | intended-system.md intended UX and flow; plan.md Demo phase dependencies | Not applicable | OR-001, OR-002, OR-006, NFR-011 | Example request and state expectations | Not applicable | Implementation Phases and Configuration/Deployment Strategy | T083, T084, T085 | TC-084 through TC-091 | Planned and traced |

## 5. Legacy Evidence to Business Rules
| Business rule | Legacy evidence source | Evidence summary | Confidence/status | Modernization note |
|---|---|---|---|---|
| BR-001 | program-analysis.md -> SQL Analysis -> SQL predicates | Exact sort code + account predicates in count and list cursors | Confirmed evidence | None |
| BR-002 | program-analysis.md -> SQL Analysis -> SQL predicates | Inclusive supplied date predicates >= and <= | Confirmed evidence | None |
| BR-003 | program-analysis.md -> SQL Analysis -> date filtering behavior | Missing fromDate uses sentinel normalization path before SQL | Confirmed evidence with runtime-effect uncertainty | Modern API omitted-boundary contract is separate decision |
| BR-004 | program-analysis.md -> SQL Analysis -> date filtering behavior | Missing toDate uses sentinel normalization path before SQL | Confirmed evidence with runtime-effect uncertainty | Modern API omitted-boundary contract is separate decision |
| BR-005 | program-analysis.md -> Inputs and Outputs -> input normalization | limit 0 normalizes to 50 | Confirmed evidence | None |
| BR-006 | program-analysis.md -> Inputs and Outputs -> input normalization | limit > 100 normalizes to 100 | Confirmed evidence | None |
| BR-007 | program-analysis.md -> Inputs and Outputs -> max transaction array size | OCCURS 100 and cap 100 constrain page size | Confirmed evidence | None |
| BR-008 | program-analysis.md -> SQL Analysis -> pagination approach | Offset applied in loop after filtering/ordering | Confirmed evidence | None |
| BR-009 | program-analysis.md -> SQL Analysis -> ordering and tie behavior | ORDER BY date DESC, time DESC; no tertiary tie key | Confirmed evidence plus tie ambiguity | No tertiary key introduced |
| BR-010 | program-analysis.md -> SQL Analysis -> COUNT vs LIST behavior | total count is pre-pagination filtered count | Confirmed evidence | None |
| BR-011 | program-analysis.md -> SQL Analysis -> COUNT vs LIST behavior | returned count equals accepted rows in page | Confirmed evidence | None |
| BR-012 | program-analysis.md -> Error-Path Analysis -> success path | SQLCODE 100 in list path yields successful empty outcome | Confirmed evidence | None |
| BR-013 | program-analysis.md -> Inputs and Outputs -> output population | success flag set on normal completion | Confirmed evidence | None |
| BR-014 | program-analysis.md -> Inputs and Outputs -> input normalization | invalid eyecatcher normalized to ITRL and continues | Confirmed evidence | Not surfaced as modern API behavior |
| BR-015 | program-analysis.md -> Inputs and Outputs -> row field details | Deterministic composite transaction ID shape | Confirmed evidence | None |
| BR-016 | mapping-matrix.md -> Core Field and Flow Mapping | Direct row-value mapping with date conversion path | Confirmed evidence with compatibility uncertainties | Contract restricts output to approved fields |
| BR-017 | program-analysis.md -> SQL Analysis -> predicates | Count and list use same filter criteria | Confirmed evidence | None |
| BR-018 | program-analysis.md -> Error-Path Analysis -> ABNDPROC invocation | SQL technical failures route to abend path; no partial success | Confirmed evidence | Modern API expresses this as HTTP 500 safe error |
| BR-019 | business-rules.md -> BR-019 | Composite ID components align with detail key fields | Reasonable inference | INQTRAND remains out of scope |

## 6. Business Rules to Requirements
| Business rule | Requirement mapping |
|---|---|
| BR-001 | FR-001, FR-010, FR-012, FR-013 |
| BR-002 | FR-002, FR-013 |
| BR-003 | FR-002, FR-013 |
| BR-004 | FR-002, FR-013 |
| BR-005 | FR-003 |
| BR-006 | FR-003 |
| BR-007 | FR-003 |
| BR-008 | FR-004 |
| BR-009 | FR-005 |
| BR-010 | FR-006 |
| BR-011 | FR-006 |
| BR-012 | FR-007 |
| BR-013 | FR-007 |
| BR-014 | Not directly mapped to FR; legacy control normalization evidence only |
| BR-015 | FR-008 |
| BR-016 | FR-009 |
| BR-017 | FR-006 |
| BR-018 | FR-011 |
| BR-019 | FR-008 |

## 7. Requirements to Specification
| Requirement(s) | Specification trace |
|---|---|
| FR-001 | spec.md -> US-001, AC-001, API Behaviour path parameters |
| FR-002 | spec.md -> US-002, AC-002, Omitted-date processing note, Edge Cases omitted fromDate/toDate |
| FR-003 | spec.md -> US-003, AC-003, Validation Rules limit |
| FR-004 | spec.md -> US-003, AC-004, AC-008, Feature Invariants offset semantics |
| FR-005 | spec.md -> US-005, AC-005, Feature Invariants ordering keys |
| FR-006 | spec.md -> US-003, AC-006, AC-008, Response Field Definitions totalCount/returnedCount |
| FR-007 | spec.md -> US-004, AC-007, AC-008, Error Handling no 404 for empty |
| FR-008 | spec.md -> AC-009, Response Field Definitions transactionId |
| FR-009 | spec.md -> AC-010, Response Field Definitions transaction row properties |
| FR-010 | spec.md -> AC-011, Feature Invariants read-only |
| FR-011 | spec.md -> US-006, AC-012, Error Handling technical failures |
| FR-012 | spec.md -> AC-013, API Behaviour and frontend behavior notes |
| FR-013 | spec.md -> AC-014, Feature Invariants, Omitted-date processing note |
| FR-014 | spec.md -> AC-015, Non-goals, Existing application compatibility invariant |
| NFR-001 through NFR-011 | spec.md -> Feature Invariants, Validation Rules, Error Handling, AC alignment where applicable |
| SR-001 through SR-004 | spec.md -> Error Handling, Validation Rules, Non-goals |
| OR-001 through OR-009 | spec.md -> Existing application compatibility, contract/runtime reconciliation note |

## 8. Requirements to OpenAPI
| Requirement(s) | OpenAPI trace |
|---|---|
| FR-001 | GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions; path params sortCode/accountNumber patterns |
| FR-002 | query params fromDate/toDate optional and pattern; operation description omitted-boundary contract note |
| FR-003 | query param limit schema minimum/default and description; TransactionListResponse limit max |
| FR-004 | query param offset schema minimum/default and description |
| FR-005 | TransactionListResponse.transactions description ordering statement |
| FR-006 | TransactionListResponse.totalCount and returnedCount schema |
| FR-007 | 200 response description and examples emptyNoMatch/emptyOffsetBeyondTotal |
| FR-008 | Transaction.transactionId schema |
| FR-009 | Transaction schema fields and constraints |
| FR-010 | operation description read-only inquiry |
| FR-011 | 500 response and ErrorResponse schema |
| FR-012 | Not applicable (transport describes backend contract; frontend behavior in spec/tasks/tests) |
| FR-013 | operation description plus omitted-boundary caveat text |
| FR-014 | Not applicable to transport contract |
| NFR-008 | ErrorResponse safe envelope contract |
| SR-002 | path/query parameter constraints |
| OR-005 | runtime reconciliation target against backend/api/src/main/resources/openapi.yaml |
| Others | Not applicable to OpenAPI transport scope |

## 9. Requirements to Plan
| Requirement(s) | Plan trace |
|---|---|
| FR-001 through FR-009 | plan.md -> Backend Component Design (Repository Design, Service Design, Mapper Design), Persistence Strategy |
| FR-010 | plan.md -> Persistence Strategy, Verified Legacy Behavior To Preserve |
| FR-011 | plan.md -> Error-Handling Strategy, Verified Legacy Behavior To Preserve |
| FR-012 | plan.md -> Frontend Design, End-to-End Data Flow |
| FR-013 | plan.md -> Plan Overview date-handling distinction, Legacy-to-Modern Transformation Strategy |
| FR-014 | plan.md -> Repository Integration Strategy, Repository-First Implementation Rule |
| NFR-001 | plan.md -> Technical Context |
| NFR-002 | plan.md -> Mapper Design, Validation Strategy |
| NFR-003, NFR-004 | plan.md -> Backend Component Design (Controller non-responsibilities, Service responsibilities) |
| NFR-005 | plan.md -> Target Architecture future substitution seam, Adapter Design |
| NFR-006 | plan.md -> Validation Strategy ownership split |
| NFR-007, NFR-008, NFR-009 | plan.md -> Logging and Observability, Security Strategy, Error-Handling Strategy |
| NFR-010, NFR-011 | plan.md -> Testing Strategy, Implementation Phases |
| SR-001 through SR-004 | plan.md -> Security Strategy, Validation Strategy, Error-Handling Strategy |
| OR-001 through OR-009 | plan.md -> Technical Context, Configuration and Deployment Strategy, API Contract Strategy |

## 10. Requirements to Implementation Tasks
| Requirement(s) | Task trace |
|---|---|
| FR-001 | T015, T020, T021, T033, T038, T059, T063, T074 |
| FR-002 | T015, T016, T020, T021, T028, T033, T038, T049, T054, T063, T073, T074, T084 |
| FR-003 | T015, T016, T027, T047, T049, T055, T062, T064, T074, T084 |
| FR-004 | T015, T016, T027, T047, T049, T055, T062, T064, T074, T084 |
| FR-005 | T021, T055, T074, T078 |
| FR-006 | T017, T019, T020, T022, T029, T030, T040, T041, T050, T054, T055, T060, T063, T074, T078 |
| FR-007 | T017, T030, T034, T041, T052, T057, T060, T064, T074, T084 |
| FR-008 | T017, T023, T051 |
| FR-009 | T011, T012, T017, T018, T023, T040, T051, T060 |
| FR-010 | T020, T021, T056, T078 |
| FR-011 | T003, T018, T024, T031, T035, T042, T052, T056, T057, T061, T065, T075, T078, T084 |
| FR-012 | T002, T004, T032, T037, T038, T039, T040, T041, T042, T043, T059, T060, T061, T062, T063, T064, T065, T066, T071, T074, T084 |
| FR-013 | T028, T067, T073, T078 |
| FR-014 | T001, T002, T008, T013, T036, T037, T043, T044, T058, T062, T066, T076, T077 |
| NFR-001 | T009 (dependency sufficiency), T080 (config conventions) |
| NFR-002 | T023, T033, T051, T059 |
| NFR-003 | T015, T017, T036, T077 |
| NFR-004 | T025, T029, T036, T077 |
| NFR-005 | T014, T019, T082 |
| NFR-006 | T016, T025, T033 |
| NFR-007 | T053 |
| NFR-008 | T003, T018, T035, T036, T042, T053, T057, T061, T065, T075, T078 |
| NFR-009 | T053 |
| NFR-010 | T007, T067, T068, T069, T079 |
| NFR-011 | T006, T009, T049 through T066, T079, T085 |
| SR-001 | T078, T079 |
| SR-002 | T025, T033, T057, T061, T065, T075 |
| SR-003 | T020, T021, T056, T078 |
| SR-004 | T003, T018, T035, T042, T053, T061, T065, T075 |
| OR-001 | T001, T032, T070, T083 |
| OR-002 | T002, T004, T037, T039, T062, T066, T071, T072, T083 |
| OR-003 | T005, T010, T011, T012, T014, T081 |
| OR-004 | T014, T073, T082 |
| OR-005 | T045, T046, T048, T058, T070 |
| OR-006 | T001, T006, T008, T009, T070, T071, T072, T080, T081, T083, T085 |
| OR-007 | T080 |
| OR-008 | T053, T078 |
| OR-009 | T001, T002, T003, T005, T013, T036, T044, T058, T066, T076, T077, T081 |

## 11. Requirements to Test Cases
| Requirement | Requirement summary | Specification | OpenAPI | Plan | Task(s) | Test(s) | Coverage status |
|---|---|---|---|---|---|---|---|
| FR-001 | Exact account filtering | AC-001 | Path params | Repository Design | T015, T020, T021, T033 | TC-001, TC-039, TC-084 | Planned and traced; Pending implementation/test execution |
| FR-002 | Inclusive supplied dates and approved omitted-boundary handling | AC-002 | fromDate/toDate optional | Service Design, Validation Strategy | T016, T028, T047 | TC-002 to TC-006, TC-040, TC-041, TC-085, TC-086 | Planned and traced; Pending implementation/test execution |
| FR-003 | limit default/cap/max | AC-003 | limit param/schema | Service Design | T027, T049, T055 | TC-007 to TC-010, TC-035, TC-036, TC-088 | Planned and traced; Pending implementation/test execution |
| FR-004 | offset after filter/order | AC-004, AC-008 | offset param/schema | Service Design | T027, T049, T055 | TC-011 to TC-015, TC-045, TC-088, TC-089 | Planned and traced; Pending implementation/test execution |
| FR-005 | date/time descending ordering only | AC-005 | TransactionListResponse ordering description | Repository Design | T021, T055, T078 | TC-042 to TC-047 | Planned and traced; Pending implementation/test execution |
| FR-006 | totalCount/returnedCount semantics and parity | AC-006, AC-008 | 200 schema counts | Service Design, Repository Design | T029, T030, T050, T054, T055 | TC-017, TC-021, TC-022, TC-039 to TC-041 | Planned and traced; Pending implementation/test execution |
| FR-007 | Empty success semantics | AC-007, AC-008 | 200 empty examples | Error-Handling Strategy | T030, T034, T052, T057 | TC-016, TC-087, TC-089 | Planned and traced; Pending implementation/test execution |
| FR-008 | Composite transaction ID | AC-009 | Transaction.transactionId | Mapper Design | T023, T051 | TC-058 | Planned and traced; Pending implementation/test execution |
| FR-009 | Approved output field mapping | AC-010 | Transaction schema | Mapper Design | T017, T018, T023, T051 | TC-051 to TC-057, TC-059 | Planned and traced; Pending implementation/test execution |
| FR-010 | Read-only behavior | AC-011 | GET operation read-only description | Persistence Strategy | T020, T021, T056 | TC-018, TC-048 | Planned and traced; Pending implementation/test execution |
| FR-011 | Technical failure no partial success | AC-012 | 500 response + ErrorResponse | Error-Handling Strategy | T024, T031, T035, T052, T057, T065 | TC-019, TC-020, TC-049, TC-050, TC-091 | Planned and traced; Pending implementation/test execution |
| FR-012 | Frontend integration and states | AC-013 | Endpoint and status behaviors used by UI | Frontend Design | T038 through T044, T059 through T066 | TC-070 to TC-093 (relevant) | Planned and traced; Pending implementation/test execution |
| FR-013 | Preserve evidenced legacy behavior unless approved modernization supersedes | AC-014 | Omitted-boundary caveat text | Legacy-to-Modern Transformation Strategy | T028, T067, T073, T078 | TC-004, TC-005, TC-006, TC-041, TC-086 | Planned and traced; Pending implementation/test execution |
| FR-014 | Integrate without unrelated regressions | AC-015 | Not applicable to transport contract | Repository Integration Strategy | T001, T008, T044, T058, T066, T076, T077 | TC-083, TC-093 | Planned and traced; Pending implementation/test execution |
| NFR-001 | Java 21 + Spring Boot 3 conventions | Feature Invariants (compatibility intent) | Not applicable to OpenAPI | Technical Context | T009, T080 | No direct TC; verified via build/runtime checks | Planned and traced |
| NFR-002 | Leading-zero preservation | Feature Invariants | Path/field patterns as strings | Mapper Design | T023, T051, T059 | TC-029, TC-051, TC-052, TC-055, TC-071 | Planned and traced; Pending implementation/test execution |
| NFR-003 | Separation of concerns | AC-015 alignment | Not applicable to OpenAPI | Backend Component Design | T015, T017, T036, T077 | No direct TC; review/integration verification | Planned and traced |
| NFR-004 | Service owns business orchestration | Feature Invariants and design intent | Not applicable to OpenAPI | Service Design | T025, T029, T036, T077 | TC-017, TC-021 | Planned and traced; Pending implementation/review execution |
| NFR-005 | Repository abstraction for future adapter | Non-goals and boundaries | Not applicable to OpenAPI | Target Architecture, Adapter Design | T014, T019, T082 | No direct TC; architectural/review verification | Covered with deferred production-adapter concern |
| NFR-006 | Validation ownership split | Validation Rules | SR-002-related transport constraints | Validation Strategy | T016, T025, T033 | TC-023 to TC-038 | Planned and traced; Pending implementation/test execution |
| NFR-007 | Logging minimization | Error Handling and non-goals | Not applicable to OpenAPI | Logging and Observability | T053 | TC-099 | Planned and traced; Pending implementation/test execution |
| NFR-008 | Safe error exposure | Error Handling | ErrorResponse 400/500 | Error-Handling Strategy | T003, T035, T042, T053, T057, T061 | TC-096, TC-097 | Planned and traced; Pending implementation/test execution |
| NFR-009 | Correlation conventions | Error Handling note | ErrorResponse.correlationId | Logging and Observability | T053 | TC-098, TC-100 | Planned and traced; Pending implementation/test execution |
| NFR-010 | End-to-end traceability maintenance | Document-level traceability intent | Not applicable to OpenAPI | Implementation Phases (verification artifacts) | T007, T067, T068, T069, T079 | No direct TC; artifact verification | Planned and traced |
| NFR-011 | Required test layers | Acceptance/testing expectations | Contract status/schema checks | Testing Strategy | T006, T049 through T066, T085 | TC-001 through TC-100 by layer assignment | Planned and traced; Pending test execution |
| SR-001 | No invented feature-specific authorization rule | Error Handling and non-goals | Not applicable to OpenAPI | Security Strategy | T078, T079 | TC-094, TC-095 | Planned and traced; Pending implementation/test execution |
| SR-002 | Structural/syntactic boundary validation | Validation Rules | Path/query constraints | Validation Strategy | T025, T033, T057, T061, T065 | TC-023 to TC-034, TC-090 | Planned and traced; Pending implementation/test execution |
| SR-003 | SQL injection prevention via parameterized queries | Validation and safe-query construction constraints | Not applicable to OpenAPI | Repository Design, Persistence Strategy | T020, T021, T056, T078 | TC-101 | Planned and traced; Pending implementation/test execution |
| SR-004 | Sensitive data not leaked in errors/logs | Error Handling | ErrorResponse schema safety | Security Strategy, Logging and Observability | T003, T018, T035, T042, T053, T061 | TC-096, TC-097, TC-099 | Planned and traced; Pending implementation/test execution |
| OR-001 | Existing backend runtime/port extension | Existing application compatibility | Not applicable to OpenAPI | Technical Context | T001, T032, T070, T083 | No direct TC; startup and docs verification | Planned and traced |
| OR-002 | Existing frontend route/navigation + proxy reuse | AC-013/AC-015 alignment | Not applicable to OpenAPI | Frontend Design | T002, T037, T039, T062, T066, T071, T083 | TC-070, TC-083, TC-093 | Planned and traced; Pending implementation/test execution |
| OR-003 | Existing H2/sql init conventions | Edge-cases and compatibility notes | Not applicable to OpenAPI | Configuration and Deployment Strategy | T005, T011, T012, T081 | No direct TC ID; integration startup checks | Planned and traced |
| OR-004 | No live DB2/CICS/mainframe in POC | Non-goals | Not applicable to OpenAPI | Technical Context, Configuration and Deployment Strategy | T014, T073, T082 | No direct TC ID; operational verification | Planned and traced |
| OR-005 | Runtime OpenAPI reconciliation before merge | Error Handling and contract notes | Runtime publication reconciliation | API Contract Strategy | T045, T046, T048, T058 | TC-069 | Planned and traced; Pending implementation/test execution |
| OR-006 | Existing deployment/environment-profile compatibility | Existing application compatibility | Not applicable to OpenAPI | Configuration and Deployment Strategy | T001, T006, T009, T070, T071, T072, T080, T083, T085 | No direct TC ID; command/run verification | Planned and traced |
| OR-007 | Existing configuration management conventions | Existing application compatibility | Not applicable to OpenAPI | Configuration and Deployment Strategy | T080 | No direct TC ID; config review verification | Planned and traced |
| OR-008 | Logging/monitoring/metrics/correlation conventions | Error Handling correlation note | ErrorResponse.correlationId where supported | Logging and Observability | T053, T078 | TC-098, TC-100 | Planned and traced; Pending implementation/test execution |
| OR-009 | Preserve unrelated endpoint/service behavior | AC-015, Non-goals | Not applicable to OpenAPI | Repository Integration Strategy, Regression Tests | T013, T044, T058, T066, T076, T077, T081 | TC-083, TC-093 | Planned and traced; Pending implementation/test execution |

## 12. Frontend Traceability
| Frontend behavior | Specification/OpenAPI authority | Repository convention authority | Task trace | Test trace | Status |
|---|---|---|---|---|---|
| Route and navigation integration | spec.md AC-013/AC-015; FR-012/FR-014 | frontend/app current route/shell patterns | T037, T044 | TC-070, TC-083, TC-093 | Planned and traced |
| Form inputs | spec.md API Behaviour and Validation Rules | frontend/app features form patterns | T038 | TC-071, TC-078 | Planned and traced |
| API-client request construction | contracts/openapi.yaml GET path and params | frontend/app src/api conventions | T039 | TC-072, TC-073 | Planned and traced |
| Optional-date omission | contracts/openapi.yaml fromDate/toDate optional | frontend/app API-client conventions | T039, T047 | TC-074, TC-086 | Planned and traced |
| Loading state | spec.md AC-013 | frontend/app component state patterns | T040 | TC-075 | Planned and traced |
| Populated results | spec.md AC-013; Transaction schema | frontend/app rendering patterns | T040 | TC-076, TC-084 | Planned and traced |
| Empty results | spec.md AC-007/AC-013; OpenAPI empty examples | frontend/app state patterns | T041 | TC-077, TC-087, TC-089 | Planned and traced |
| Validation feedback | spec.md Validation Rules | frontend/app boundary feedback patterns | T042 | TC-078, TC-090 | Planned and traced |
| Technical-error state | spec.md Error Handling; ErrorResponse | frontend/app error-display conventions | T042 | TC-079, TC-091 | Planned and traced |
| Metadata and row rendering | spec.md Response Field Definitions | frontend/app component conventions | T040, T041 | TC-076, TC-081 | Planned and traced |
| Pagination behavior | spec.md AC-003/AC-004/AC-006 | frontend/app controls/state conventions | T041, T062 | TC-081, TC-088 | Planned and traced |
| Subsequent inquiry replacement | spec.md AC-013/AC-015 | frontend/app state-transition conventions | T043, T062, T066 | TC-082, TC-092 | Planned and traced |
| Shared shell/route regression | FR-014 and OR-009 guardrails | frontend/app existing shell/routes | T044, T062, T066, T076 | TC-083, TC-093 | Planned and traced |

## 13. Error, Security, and Operational Traceability
| Concern | Requirement(s) | Source trace | Plan trace | Task trace | Test trace | Status |
|---|---|---|---|---|---|---|
| Count-stage technical failure mapping | FR-011, BR-018 | program-analysis.md -> Error-Path Analysis | Error-Handling Strategy | T024, T031, T035 | TC-019, TC-049 | Planned and traced |
| Row-stage technical failure mapping | FR-011, BR-018 | program-analysis.md -> Error-Path Analysis | Error-Handling Strategy | T024, T031, T035 | TC-020, TC-050 | Planned and traced |
| No partial successful page | FR-011, BR-018 | spec.md Feature Invariants and AC-012 | Error-Handling Strategy | T031, T052 | TC-019, TC-020, TC-091 | Planned and traced |
| Safe error envelopes | NFR-008, SR-004 | spec.md Error Handling | Error-Handling Strategy | T018, T035, T042, T053 | TC-096, TC-097 | Planned and traced |
| No SQL/stack leakage | NFR-008, SR-004 | supporting/requirements.md NFR-008/SR-004 | Error-Handling Strategy | T035, T042, T053 | TC-096, TC-097, TC-080 | Planned and traced |
| Existing route-security alignment | SR-001, OR-009 | supporting/requirements.md SR-001/OR-009 | Security Strategy | T078, T079 | TC-094, TC-095 | Planned and traced |
| No invented feature authorization | SR-001 | supporting/requirements.md SR-001 | Security Strategy | T078, T079 | TC-095 | Planned and traced |
| Logging minimization | NFR-007, SR-004 | supporting/requirements.md NFR-007 | Logging and Observability | T053, T078 | TC-099 | Planned and traced |
| Correlation/request identification | NFR-009, OR-008 | supporting/requirements.md NFR-009/OR-008 | Logging and Observability | T053, T078 | TC-098, TC-100 | Planned and traced |
| H2 initialization and startup | OR-003, OR-006, OR-009 | supporting/requirements.md operational section | Configuration and Deployment Strategy | T005, T011, T012, T081 | No direct standalone TC; startup evidence planned | Planned and traced |
| Runtime OpenAPI publication | OR-005 | supporting/requirements.md OR-005 | API Contract Strategy | T046, T058 | TC-069 | Planned and traced |
| Regression protection | FR-014, OR-009 | spec.md AC-015 | Testing Strategy (Regression Tests) | T058, T076 | TC-083, TC-093 | Planned and traced |
| Documentation and quickstart | OR-001, OR-002, OR-006 | supporting/requirements.md operational section | Implementation Phases phase 8 | T070, T071, T072, T073 | No direct standalone TC; documentation evidence planned | Planned and traced |
| Automated test commands | NFR-011, OR-006 | supporting/requirements.md NFR-011/OR-006 | Testing Strategy | T006, T085 | No direct standalone TC; command log evidence planned | Planned and traced |
| Demo workflow | FR-002, FR-003, FR-004, FR-007, FR-011, FR-012 | spec.md AC set and Edge Cases | Implementation Phases | T083, T084 | TC-084 through TC-091 | Planned and traced |

## 14. Modernization Decision Traceability
| Decision | Legacy evidence status | Approved source | Requirement/spec trace | Implementation trace | Test trace | Limitation |
|---|---|---|---|---|---|---|
| Optional omitted date boundaries at API boundary | Legacy sentinel normalization is verified; unconstrained deployed SQL outcome is not verified | supporting/requirements.md -> Modernization Enhancements Requiring Approval; plan.md -> Plan Overview and Legacy-to-Modern Transformation Strategy; contracts/openapi.yaml fromDate/toDate descriptions | FR-002, FR-013; spec.md AC-002, AC-014 and Omitted-date processing note | T016, T028, T039, T047, T067, T073, T078 | TC-004, TC-005, TC-006, TC-041, TC-074, TC-086 | Modern tests verify target contract only, not legacy deployed SQL equivalence |
| HTTP/JSON API semantics | Not legacy COMMAREA-equivalent by transport; approved modernization | research.md Decision: REST-Style Backend Boundary; spec.md API Behaviour; openapi.yaml | FR-011, FR-012; AC-012, AC-013 | T032, T034, T035, T045, T048 | TC-060 through TC-068, TC-090, TC-091 | Transport modernization is intentional and not claimed as legacy transport equivalence |
| Explicit limit and offset API controls | Legacy behavior exists; explicit HTTP query expression is modernization | research.md Decision: Pagination Preservation Strategy; spec.md API Behaviour | FR-003, FR-004; AC-003, AC-004 | T015, T027, T033, T039, T047 | TC-007 through TC-015, TC-062, TC-063, TC-073 | No tertiary ordering key introduced |
| H2/JDBC proof-of-concept persistence | Legacy runtime is DB2/CICS; H2/JDBC is approved modernization for POC | research.md Decision: H2 for Local Development and Demonstration; supporting/requirements.md OR-003/OR-004 | OR-003, OR-004 | T011, T012, T014, T020, T021, T082 | Repository behavior covered by TC-048 through TC-050; startup/configuration validated via task evidence, no direct standalone TC | Does not prove production DB2 behavior |
| React frontend inquiry behavior | Legacy UI/navigation not authoritative for modern implementation | plan.md Frontend Design and source precedence; supporting/requirements.md FR-012/FR-014 | FR-012, FR-014; AC-013, AC-015 | T037 through T044 | TC-070 through TC-093 | Frontend behavior authority is Feature 005 spec/OpenAPI, not historical INQCUST-specific package content |
| Future adapter seam | Legacy static artifacts do not define modern adapter ownership/config | plan.md Target Architecture and Future Mainframe Adapter Options; architecture.md section 15 | NFR-005, OR-004 | T014, T019, T082 | Architectural/review evidence; no live adapter TC in scope | Production adapter ownership/configuration remains deferred |

## 15. Coverage Summary
Computed from this rebuilt matrix and finalized source artifacts:
- Total verified requirements (FR + NFR + SR + OR): 38.
- Requirements with explicit implementation-task coverage links: 38.
- Requirements with direct TC-### test-case coverage links: 29.
- Requirements verified primarily through review/configuration/documentation evidence (no direct standalone TC): 9.
- Requirements with OpenAPI applicability: 13.
- Requirements not applicable to OpenAPI transport scope: 25.
- Business rules traced to evidence: 19 (BR-001 through BR-019).
- Modernization decisions explicitly separated: 6.
- Remaining explicit requirement-to-task trace gaps: 0.
- Remaining requirement-to-direct-TC gaps: 9 (covered by planned review/configuration/documentation evidence).

Status interpretation:
- Coverage is planned and traced.
- Implementation/test/QA execution evidence is pending.

## 16. Gaps, Risks, and Deferred Traceability
Visible unresolved items (not silently closed):
- Deployed DB2 schema/type/nullability uncertainty remains unresolved (program-analysis.md and data-model.md uncertainty sections).
- Omitted-date legacy runtime outcome remains unresolved; matrix preserves separation between verified sentinel path and approved modern optional-boundary contract behavior.
- No tertiary ordering key guarantee exists for date/time ties.
- Future production adapter ownership and configuration remain deferred.
- Production DB2/mainframe verification remains deferred outside Feature 005 POC scope.

Explicit requirement-to-task traceability gaps for FR-013, NFR-004, SR-001, SR-003, and OR-008 are closed.

Remaining non-contradictory limitations are evidence-type boundaries (for example, operational/configuration/doc requirements that do not have direct standalone TC scenarios).

## 17. Maintenance Rules
- Update this matrix whenever requirements change.
- Update this matrix whenever business rules change.
- Update this matrix whenever OpenAPI changes.
- Update this matrix whenever task IDs change.
- Update this matrix whenever tests are added or removed.
- Update this matrix whenever implementation evidence status changes.
- Update this matrix whenever deferred adapter decisions are resolved.
- Propagate every change across all affected columns; never update only one column in isolation.

Status rules for this artifact state:
- Do not mark implementation tasks complete here.
- Do not claim tests have passed here.
- Do not claim QA or code review is complete here.
- Distinguish planned coverage from executed evidence.
