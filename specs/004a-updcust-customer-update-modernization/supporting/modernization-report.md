# Modernization Report: UPDCUST

## Summary
UPDCUST is suitable for modernization with high parity confidence because business rules are explicit and fail-code outcomes are clearly observable in COBOL.

## What Is Straightforward
- Copybook-constrained field mapping.
- Key-based read and update flow.
- Fail-code table implementation.
- Legacy status propagation in API response.

## What Needs Care
- Preserving selective gate behavior exactly (especially title and address gates).
- Handling blank and spaced values consistently.
- Maintaining sortCode fallback semantics when not provided.

## Recommended Delivery Order
1. Backend domain/service parity.
2. Controller and error mapping.
3. Inquiry-to-update UX integration.
4. Full parity and regression testing.

## Readiness
Specification package is implementation-ready pending user approval of API path style and sortCode transport (query vs body).
