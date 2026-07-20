# Frontend Code Review Rules

Use these rules when reviewing frontend changes. The review should help a
manager, QA lead, designer, or dev lead understand whether the user-facing
change is ready for human approval.

## Review Stance

- Treat the spec, plan, tasks, and design requirements as the intended contract.
- Review the changed UI against user workflow, accessibility, state handling,
  testability, maintainability, and performance.
- Prefer concrete findings with file paths, line references, and evidence.
- Do not approve or reject the PR. Produce a decision-support report.
- Do not modify source code unless the user explicitly asks for safe fixes.

## Blocking Frontend Concerns

Flag a BLOCKER when any of these are true:

- A critical user workflow cannot be completed.
- The UI exposes sensitive data or authorization-protected data.
- The change introduces an XSS risk through unsafe HTML rendering, URL handling,
  script injection, or unsanitized user-controlled content.
- The UI cannot be operated by keyboard for a required workflow.
- A form can submit invalid or destructive data without clear validation or
  confirmation.
- The change breaks existing routes, deep links, or navigation required by users.

## Major Frontend Concerns

Flag a MAJOR issue when any of these are true:

- Missing tests for changed user behavior, form validation, error states, or
  key conditional rendering.
- Loading, empty, error, and success states are incomplete or inconsistent.
- Accessibility basics are missing: labels, focus management, semantic controls,
  color contrast, or screen-reader names.
- Client-side authorization or feature gating is used without server-side
  enforcement where needed.
- API errors are swallowed or shown as vague messages that QA cannot verify.
- The UI can double-submit a mutation or create duplicate side effects.
- New code creates likely performance problems through avoidable re-renders,
  large bundles, unbounded lists, or expensive work in render paths.
- Layout breaks or text overlaps at common mobile or desktop viewport sizes.

## Minor Frontend Concerns

Flag a MINOR issue when any of these are true:

- Component naming, folder placement, or styling does not match nearby code.
- Visual polish issues exist but do not block the workflow.
- Repeated UI logic could be simplified without changing behavior.
- Analytics, telemetry, or UX copy could be clearer.
- Tests exist but are too coupled to implementation details.

## Required Review Areas

For each frontend change, inspect:

- User workflow: happy path, failure path, cancellation, retry, and undo.
- Accessibility: keyboard, focus, labels, ARIA only where needed, contrast.
- Security: XSS, unsafe redirects, sensitive data exposure, feature gating.
- State handling: loading, empty, error, success, optimistic updates, stale data.
- Forms: validation, disabled states, duplicate submissions, error placement.
- API integration: request lifecycle, error mapping, retries, cache invalidation.
- Responsiveness: mobile, tablet, desktop, text wrapping, layout stability.
- Performance: render paths, memoization only where useful, bundle impact.
- Tests: user-level tests, validation tests, error-state tests, accessibility checks.
- Spec traceability: each meaningful UI requirement has implementation evidence.

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
- Owner recommendation: Dev, QA, Dev Lead, Design, Security, or Product
