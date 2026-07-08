# INQCUST Modernization Constitution

## Core Principles

### I. Specification First
The specification is the source of truth. Code exists only to satisfy the approved feature specification. If behavior changes, update the spec before implementation.

### II. Preserve Legacy Behavior
The modernized service must preserve observable INQCUST inquiry behavior for supported POC flows: specific customer lookup, random customer lookup, latest customer lookup, inquiry success flag, and inquiry failure code.

### III. Bounded Strangler Replacement
The project modernizes one bounded capability, INQCUST customer inquiry. It must not expand into full Bank of Z modernization, account management, customer update, authentication, or live mainframe integration.

### IV. Adapter Boundary Required
All mainframe or data access must go through the CustomerRepository interface. The POC implementation uses mock data only. Future DB2, CICS, z/OS Connect, or MQ adapters may replace the mock repository without changing controller behavior.

### V. Testable Business Rules
Every functional requirement and business rule must have deterministic tests. Random customer behavior must be made deterministic in tests.

### VI. Contract Alignment
The implementation must match the OpenAPI contract and field mapping matrix exactly. No extra customer fields should be invented.

### VII. AI Guardrails
GitHub Copilot prompts must reference the spec, plan, tasks, mapping matrix, and test spec. Copilot output must be reviewed against these artifacts before being accepted.

## Governance
- Specs are reviewed before implementation.
- Technical plan is reviewed before task execution.
- Tasks must reference specific requirements or business rules.
- Tests must be written for positive, negative, and edge-case behavior.
- Demo changes must be represented as spec changes before code changes.
