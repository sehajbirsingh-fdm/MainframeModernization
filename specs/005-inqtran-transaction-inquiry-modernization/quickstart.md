# Quickstart: Validate Temporary Plan Artifacts (INQTRAN)

This guide validates placeholder planning artifacts only.
It does not validate implementation behavior.

## Prerequisites

- Access to repository root.
- PowerShell terminal.

## Setup Commands

1. Confirm active feature pointer:
   - Get-Content .specify/feature.json
2. Confirm feature directory exists:
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization

## Validation Commands

1. Validate required planning files exist:
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/spec.md
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/plan.md
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/research.md
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/data-model.md
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/quickstart.md
2. Validate contracts placeholder exists:
   - Test-Path specs/005-inqtran-transaction-inquiry-modernization/contracts/placeholder-contract.md

## Expected Outcomes

- All listed files return True.
- plan.md states implementation is blocked for this temporary phase.
- research.md states unresolved decisions are deferred to legacy analysis and approved specification replacement.
- No runtime endpoints, schemas, tables, SQL, or business rules are defined in these placeholder artifacts.

## Important Note

This quickstart confirms workflow readiness only.
Do not start implementation from this temporary plan.
