# Research: INQTRAN Transaction Inquiry Modernization (Temporary Placeholder)

## Decision 1
Decision: Treat all business behavior as provisional and not yet approved.
Rationale: The current feature specification is explicitly a workflow placeholder.
Alternatives considered:
- Infer behavior from prior inquiry features: rejected due to risk of inventing unsupported rules.
- Start implementation with assumptions: rejected because temporary artifacts must not authorize implementation.

## Decision 2
Decision: Use INQTRANL.cbl as the primary legacy evidence source for final behavior extraction.
Rationale: This source is explicitly identified as primary in the temporary specification.
Alternatives considered:
- Treat INQTRAND.cbl as co-equal source now: rejected because relationship remains unconfirmed.
- Ignore legacy evidence and design forward: rejected as non-compliant with placeholder constraints.

## Decision 3
Decision: Keep INQTRAND.cbl relationship status unconfirmed.
Rationale: No verified linkage has been established yet.
Alternatives considered:
- Assume INQTRAND.cbl dependency: rejected due to missing evidence.
- Exclude INQTRAND.cbl permanently: rejected until legacy analysis confirms or denies relevance.

## Decision 4
Decision: Defer API contract specifics, payload structures, status mapping, and endpoint paths.
Rationale: These details are unresolved and must come from approved specification and legacy-derived evidence.
Alternatives considered:
- Draft provisional endpoint and schemas: rejected because this would invent interface behavior.
- Copy patterns from other features: rejected because behavior parity is not yet established.

## Decision 5
Decision: Defer architecture, testing strategy, integration approach, and migration sequencing.
Rationale: Final planning must be rebuilt from corrected specification, supporting artifacts, repository evidence, and legacy analysis.
Alternatives considered:
- Freeze a technical stack now: rejected as a premature final architecture decision.
- Authorize implementation tasks now: rejected by plan purpose and acceptance constraints.

## Clarification Resolution Summary

Items marked NEEDS CLARIFICATION in plan.md are resolved in this temporary research artifact as deferred decisions pending legacy analysis and approved specification replacement.

This is a workflow-valid resolution for placeholder planning only, not a business or implementation approval.
