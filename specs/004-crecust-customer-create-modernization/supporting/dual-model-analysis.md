# Dual-Model Analysis - CRECUST

## Purpose
Cross-validate modernization outputs from:
- Model A: strict copybook and COBOL behavioral extraction.
- Model B: API-first domain modeling for maintainability.

## Consensus Findings
- Customer creation is a stateful write flow, not inquiry.
- Legacy fail-code semantics are crucial for migration confidence.
- Customer number generation must be deterministic by sortcode.
- Date conversions must retain integer-storage and ISO-response duality.

## Potential Divergence Areas
- Title padding normalization differences.
- Credit score fallback timing/randomization behavior.
- Treatment of proctran-write failure (legacy ABEND) in modern API.

## Resolution Strategy
- Treat `spec.md` and `mapping-matrix.md` as canonical for implementation.
- Add explicit tests for each divergence-sensitive path.
- Record any intentional deviations in review reports before coding.
