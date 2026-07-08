# Implementation Plan: INQCUST Customer Inquiry Modernization

**Branch**: `001-inqcust-customer-inquiry-modernization` | **Date**: 2026-07-07 | **Spec**: `spec.md`  
**Input**: Feature specification from `/specs/001-inqcust-customer-inquiry-modernization/spec.md`

## Summary

Modernize the legacy `INQCUST.cbl` customer inquiry capability into a Java 21 Spring Boot 3 REST API. The POC uses mocked customer records instead of real CICS/DB2 connectivity and preserves legacy observable behavior through `LegacyInquiryStatus`.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3, Spring Web, Spring Validation, Spring Actuator, springdoc-openapi, JUnit 5, MockMvc  
**Storage**: Mock JSON data loaded from classpath; no real database  
**Testing**: JUnit 5, Mockito where needed, Spring MockMvc  
**Target Platform**: Local developer machine / VS Code demo environment  
**Project Type**: Web service / API modernization POC  
**Performance Goals**: Local mock response under 200ms  
**Constraints**: No real CICS, IMS, MQ, z/OS Connect, or DB2 during the POC  
**Scale/Scope**: One bounded customer inquiry capability from Bank of Z

## Constitution Check

- **Specification First**: PASS - behavior is defined in `spec.md`.
- **Preserve Legacy Behavior**: PASS - legacy status values and special customer numbers are represented.
- **Bounded Strangler Replacement**: PASS - only INQCUST customer inquiry is in scope.
- **Adapter Boundary Required**: PASS - data access goes through `CustomerRepository`.
- **Testable Business Rules**: PASS - detail captured in `test-spec.md`.
- **Contract Alignment**: PASS - OpenAPI and mapping matrix included.
- **AI Guardrails**: PASS - Copilot instructions and prompts included.

## Project Structure

### Documentation for this feature

```text
specs/001-inqcust-customer-inquiry-modernization/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── tasks.md
├── contracts/
│   └── openapi.yaml
├── checklists/
│   └── requirements.md
└── supporting/
    ├── api-contract.md
    ├── architecture.md
    ├── copilot-quality-checklist.md
    ├── data-dictionary.md
    ├── domain-model.md
    ├── mapping-matrix.md
    ├── test-spec.md
    └── traceability-matrix.md
```

### Source Code target structure

```text
src/main/java/com/fdm/bankofz/customerinquiry/
├── CustomerInquiryApplication.java
├── controller/
├── dto/
├── enums/
├── exception/
├── mapper/
├── model/
├── repository/
├── service/
└── util/

src/main/resources/
├── application.yml
└── mock-data/customer-records.json

src/test/java/com/fdm/bankofz/customerinquiry/
├── controller/
├── service/
└── util/
```

## Phase 0: Research

See `research.md`. Key decisions:
- Use mock repository because mainframe runtime is unavailable.
- Use repository interface to preserve future adapter path.
- Use REST resource path aligned with legacy key fields.
- Preserve legacy status as part of JSON response.

## Phase 1: Design

See:
- `data-model.md`
- `contracts/openapi.yaml`
- `quickstart.md`
- supporting mapping and test documents

## Implementation Approach

1. Create Spring Boot skeleton.
2. Implement DTO/domain model exactly from data model.
3. Implement repository interface and mock repository.
4. Implement lookup mode resolution.
5. Implement service orchestration for SPECIFIC, RANDOM, and LATEST.
6. Implement mapping and date conversion.
7. Implement risk assessment enhancement.
8. Implement controller and validation.
9. Implement tests from `supporting/test-spec.md`.
10. Verify with quickstart commands.

## Complexity Tracking

No constitution violations expected. The only notable complexity is random customer behavior, which is mitigated by injecting deterministic selection in tests.
