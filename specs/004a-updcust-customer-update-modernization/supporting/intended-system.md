# Intended System: UPDCUST Modernization

## System Intent
Provide a reliable customer update capability preserving UPDCUST semantics while fitting existing Spring Boot + React architecture.

## Backend Intent
- Endpoint receives update intent for one customer.
- Service applies legacy parity rules and fail-code mappings.
- Repository reads existing row, applies gated field updates, persists, and returns updated model.

## Frontend Intent
- User discovers customer via inquiry.
- Update button appears in successful inquiry context.
- Edit form prefilled from selected customer.
- Submit displays success or mapped failure with clear messages.

## Operational Intent
- Deterministic validation paths.
- Full traceability from request -> legacy rule -> outcome.
- No runtime dependency on mainframe in POC.
