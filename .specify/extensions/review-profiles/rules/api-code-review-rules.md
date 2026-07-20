# API Code Review Rules

Use these rules when reviewing API changes. The review should help a manager,
QA lead, or dev lead understand whether the change is ready for human approval.

## Review Stance

- Treat the spec, plan, tasks, and constitution as the intended contract.
- Review the changed code against that contract, not only against generic style.
- Prefer concrete findings with file paths, line references, and evidence.
- Do not approve or reject the PR. Produce a decision-support report.
- Do not modify source code unless the user explicitly asks for safe fixes.

## Blocking API Concerns

Flag a BLOCKER when any of these are true:

- A public API contract is broken without an explicit migration or versioning plan.
- A new or changed endpoint lacks authentication or authorization.
- User input reaches business logic, storage, queries, shell commands, or outbound calls without validation.
- Error responses expose stack traces, secrets, tokens, PII, or sensitive business data.
- A data migration can lose, corrupt, or orphan customer data.
- The code changes security-sensitive behavior without tests.

## Major API Concerns

Flag a MAJOR issue when any of these are true:

- API behavior changed but OpenAPI, schema, or contract docs were not updated.
- Tests are missing for changed behavior, validation failure, auth failure, or backward compatibility.
- Status codes or error response shapes are inconsistent with existing API patterns.
- Collection endpoints can return unbounded results without pagination or limits.
- Write endpoints that may be retried are not idempotent or do not document retry behavior.
- New integrations lack timeout, retry, circuit-breaker, or failure handling appropriate to the repo.
- New code creates likely N+1 queries, excessive network calls, or avoidable hot-path work.

## Minor API Concerns

Flag a MINOR issue when any of these are true:

- Naming, folder placement, or implementation style does not match nearby code.
- Logs or metrics are useful but incomplete.
- Comments explain obvious code, or missing comments hide non-obvious API behavior.
- The change works but is harder to maintain than needed.

## Required Review Areas

For each API change, inspect:

- Contract: endpoints, methods, request shape, response shape, status codes.
- Compatibility: API consumers relying on old behavior, default values, field removal, enum changes.
- Security: authentication, authorization, input validation, data exposure.
- Tests: unit, integration, contract/schema, regression, and negative-path tests.
- Observability: structured logs, request IDs, metrics, tracing, audit events.
- Performance: pagination, query shape, caching, batching, timeout behavior.
- Operations: rollout, rollback, migration, feature flag, config, monitoring.
- Spec traceability: each meaningful task has implementation evidence.

## Output Rules

Use this severity scale:

- BLOCKER: should stop merge until fixed.
- MAJOR: should be fixed before merge unless explicitly accepted by a human lead.
- MINOR: useful improvement, not normally merge-blocking.
- INFO: observation, assumption, or follow-up.

Every finding should include:

- Severity
- Title
- Evidence
- Why it matters
- Suggested fix
- Owner recommendation: Dev, QA, Dev Lead, Security, or Product
