# intended-system.md

## Purpose

Define the intended modernized system for INQACCCU at a business/system level while preserving confirmed legacy behavior.

## Intended System Statement

The intended system is a modern inquiry capability that includes the existing frontend application and backend inquiry API. Together they accept a customer-number inquiry and return the same business outcome currently produced by INQACCCU, with equivalent status signaling and account data semantics.

The frontend acts as the user-facing inquiry channel and interacts with the backend API to submit inquiries and present inquiry outcomes.
The frontend is a usable demonstration channel for the preserved legacy inquiry capability and does not introduce new business functionality or non-legacy inquiry modes.

## Preserved Legacy Business Behavior

1. Inquiry and validation flow
- Customer number remains the inquiry key.
- Customer validation remains a mandatory step before account retrieval, consistent with the INQCUST validation behavior.

2. Internally derived sort code
- Sort code remains internally derived as fixed value `987654` for account retrieval behavior.
- Sort code is not treated as caller-supplied inquiry input.

3. Outcome semantics
- Customer-not-found path remains distinct from customer-found outcomes.
- Customer found with zero accounts remains a valid successful outcome.

4. Status semantics
- Legacy-style status behavior is preserved, including:
  - success/failure signaling
  - customer-found indicator
  - account-count indicator
  - failure-code behavior for customer validation and cursor open/fetch/close failure paths (`1`,`2`,`3`,`4`)

5. End-of-data semantics
- End-of-data behavior equivalent to legacy `SQLCODE +100` remains normal completion, not an error condition.

6. Read-only inquiry behavior
- Inquiry behavior remains read-only in business terms.

## Preserved Data Behavior

1. Account information returned
- The intended system preserves the full legacy account information set:
  - eyecatcher
  - customer number
  - sort code
  - account number
  - account type
  - interest rate
  - opened date
  - overdraft limit
  - last statement date
  - next statement date
  - available balance
  - actual balance

2. Identifier handling
- Customer number and account number are treated as fixed-width identifiers.
- Leading zeroes are preserved.

3. Date semantics
- Legacy meaning is preserved where account dates are sourced from DB2 date values and represented in legacy output semantics as `DDMMYYYY`.
- Any future interface-level date representation changes are treated as representation-layer concerns, not business-rule changes.

4. Record-count and ordering constraints
- Returned account records remain constrained to maximum 20 per inquiry to preserve legacy behavior.
- No deterministic ordering is implied unless explicitly introduced by future approved scope.

## Modernization Objectives (Non-Prescriptive)

- Provide a modern interface channel for the same inquiry business capability.
- Improve maintainability and clarity by separating business behavior from presentation concerns.
- Preserve legacy behavior as the baseline, while allowing future approved enhancements to be considered explicitly.

## User-Channel Scope

- The approved modernization scope includes both:
  - backend inquiry API behavior
  - existing frontend user interaction behavior for inquiry submission and outcome display
- Frontend-visible outcomes are consistent with the preserved backend business semantics.
- Users can perform additional inquiries by updating inquiry input and submitting another request through normal inquiry interaction.

## Scope Boundaries

This intended-system document does not define:

- implementation frameworks or code structure
- interface path/contract details
- security implementation choices
- deployment or infrastructure topology
- technology stack commitments
- task-level delivery planning

## Possible Future Enhancements (Not Approved Scope)

- Expanding beyond the legacy 20-record response bound.
- Introducing explicit ordering behavior where none exists today.
- Alternative external date representations for client-facing channels.

These are future considerations only and are not part of the preserved legacy baseline.

## Unresolved Modernization Questions

1. What external representation conventions should be approved for identifiers and dates while preserving legacy semantics?
2. Should future scope intentionally change the legacy 20-record response bound?
3. Should future scope define deterministic ordering for returned account records?
4. What governance process will approve any business-impacting divergence from current legacy behavior?