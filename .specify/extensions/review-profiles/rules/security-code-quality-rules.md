# Security And Code Quality Review Rules

Use these rules when reviewing any code change for security vulnerabilities,
code smells, maintainability issues, and missing tests.

## Review Stance

- Treat the code diff as production-bound until proven otherwise.
- Look for concrete risks, not theoretical complaints.
- Prefer findings that include exact evidence and a practical fix.
- Distinguish security vulnerabilities from general maintainability concerns.
- Do not rewrite code unless the user explicitly asks for safe fixes.

## Blocking Security Concerns

Flag a BLOCKER when any of these are true:

- Secrets, tokens, passwords, API keys, sensitive certificates, or credentials are
  committed or logged.
- User-controlled input reaches SQL, NoSQL, shell commands, templates, HTML, URLs,
  file paths, or external calls without appropriate validation, encoding, or
  parameterization.
- Authentication or authorization is removed, bypassed, or inconsistently applied.
- Sensitive data is exposed in API responses, logs, analytics, frontend state,
  browser storage, error messages, or telemetry.
- File upload, download, or path handling enables traversal, unsafe file types,
  overwrite, or remote code execution risk.
- Cryptography, token validation, session handling, or permission checks are
  implemented ad hoc instead of using approved platform helpers.

## Major Security And Quality Concerns

Flag a MAJOR issue when any of these are true:

- Missing tests for changed security-sensitive behavior.
- Missing negative tests for validation, auth failure, permission failure, or
  malformed inputs.
- New code has high cyclomatic complexity, deep nesting, duplicated business
  logic, or unclear ownership boundaries.
- Error handling hides failures, retries forever, drops data, or masks user impact.
- New dependencies are unnecessary, unmaintained, broad in scope, or introduce
  avoidable supply-chain risk.
- Resource handling can leak memory, file handles, database connections, timers,
  subscriptions, or background jobs.
- Concurrency, idempotency, or transaction boundaries can cause duplicate writes,
  lost updates, or partial state.
- Logging lacks useful context or logs sensitive values.

## Minor Security And Quality Concerns

Flag a MINOR issue when any of these are true:

- Names, abstractions, or file placement make the code harder to understand.
- Comments explain obvious code or missing comments hide non-obvious behavior.
- Tests are present but brittle, overly broad, or too coupled to implementation.
- Small duplication or style inconsistency reduces maintainability.
- Observability could be improved without blocking delivery.

## Required Review Areas

For every change, inspect:

- Security: injection, XSS, auth, authorization, secrets, sensitive data, SSRF,
  redirects, file handling, dependency risk.
- Tests: happy path, negative path, regression, security-sensitive behavior,
  contract/schema where relevant.
- Code smells: duplication, long methods, unclear boundaries, hidden side effects,
  global state, tight coupling, dead code.
- Reliability: errors, retries, timeouts, transactions, idempotency, concurrency.
- Maintainability: naming, locality, readability, use of existing helpers.
- Observability: useful logs, metrics, traces, request IDs, audit events.
- Spec traceability: changed behavior maps to requirements and tasks.

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
