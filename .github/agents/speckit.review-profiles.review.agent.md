---
description: Review changes against Spec Kit artifacts and selected organization standards
---


<!-- Extension: review-profiles -->
<!-- Config: .specify/extensions/review-profiles/ -->
# Spec Kit Review Profiles

Run an automated, profile-based review after implementation. This command is meant
to produce a practical review package for a manager, QA lead, or dev lead.

## User Input

$ARGUMENTS

Optional arguments:

- A feature directory, such as `specs/003-update-customer-api`
- A base branch, such as `--base main`
- A review profile, such as `--profile api`, `--profile frontend`,
  `--profile security`, or `--profile fullstack`
- `--report-only` to produce findings without source edits
- `--fix-safe` to allow only tiny, low-risk fixes after reporting findings
- `--no-report-file` to return findings in chat only, if explicitly requested

## Goal

Review the current code changes against the selected review profile:

1. The active feature spec, plan, tasks, and any checklist files
2. The project constitution in `.specify/memory/constitution.md`
3. This extension's policy files:
   - `.specify/extensions/review-profiles/review-profiles-config.yml`
   - the profile rule files listed in `profiles.<profile>.rule_files`
4. Repository review instructions, if present:
   - `.github/copilot-instructions.md`
   - `.github/instructions/**/*.instructions.md`
   - `.github/pull_request_template.md`

## Operating Mode

Default to report-only mode. Do not modify application source code unless the
user explicitly passes `--fix-safe` or directly asks for fixes.

Even in `--fix-safe` mode:

- Fix only obvious, low-risk issues such as a missing test name typo or a small
  documentation inconsistency.
- Do not change API behavior, security logic, migrations, or data contracts.
- Report all larger issues instead of changing them.

## Review Profiles

The command supports profile-specific review instructions. Read
`.specify/extensions/review-profiles/review-profiles-config.yml` and choose the profile in
this order:

1. If `$ARGUMENTS` includes `--profile <name>`, use that profile.
2. Else use `review.default_profile` from the config file.
3. Else use `api`.

Common profiles:

- `api`: API contract, compatibility, security, tests, OpenAPI/schema, auth,
  validation, error contracts, observability, rollout risk.
- `frontend`: user workflow, accessibility, state handling, forms, XSS risk,
  responsive layout, frontend tests.
- `security`: vulnerabilities, missing negative tests, secrets, injection, code
  smells, maintainability.
- `fullstack`: combined API, frontend, security, test, and operational review.

If a requested profile does not exist, stop and tell the user which profiles are
available. Do not silently fall back to another profile.

Load every file listed in `profiles.<profile>.rule_files`. If a rule file is
missing, record it as a MAJOR finding because the review policy is incomplete.

## Step 1: Identify Review Scope

Find the feature directory:

1. If `$ARGUMENTS` includes a `specs/...` path, use that.
2. Else if the environment or repo indicates an active Spec Kit feature, use it.
3. Else inspect the current git branch and look for a matching `specs/<branch>`
   directory.
4. Else use the newest directory under `specs/` that contains `spec.md`,
   `plan.md`, or `tasks.md`.
5. If no feature directory exists, continue with code-diff review and say that
   no Spec Kit feature artifacts were found.

Find the code diff:

1. Prefer a diff against the base branch from `--base`; otherwise use the
   configured `context.preferred_base_branch`; otherwise try `main`.
2. Include uncommitted changes if configured.
3. Summarize changed files before reviewing details.

Useful commands:

```bash
git status --short
git branch --show-current
git diff --stat main...HEAD
git diff --name-only main...HEAD
git diff main...HEAD
git diff
```

Use PowerShell equivalents when needed on Windows.

## Step 2: Load Review Inputs

Read the available artifacts:

- `spec.md`
- `plan.md`
- `tasks.md`
- `checklists/*.md`
- `.specify/memory/constitution.md`
- `.specify/extensions/review-profiles/review-profiles-config.yml`
- all selected profile rule files, for example:
  - `.specify/extensions/review-profiles/rules/api-code-review-rules.md`
  - `.specify/extensions/review-profiles/rules/frontend-code-review-rules.md`
  - `.specify/extensions/review-profiles/rules/security-code-quality-rules.md`
- Relevant source files and tests touched by the diff

If an artifact is missing, do not fail the command. Record the missing artifact
as an INFO or MAJOR finding depending on impact.

## Step 3: Review Against Selected Profile Rules

Always review for:

- Security vulnerabilities
- Missing tests, especially negative-path tests
- Code smells and maintainability risks
- Spec/task traceability

For API profiles, review the implementation for:

- API contract compatibility
- Endpoint authentication and authorization
- Input validation and output shaping
- Error status codes and error response consistency
- Sensitive data exposure in logs, errors, responses, and telemetry
- OpenAPI, schema, or contract documentation updates
- Required tests for happy path, auth failure, validation failure, and
  backward compatibility
- Pagination, idempotency, retry behavior, and timeout handling
- Observability through logs, metrics, traces, request IDs, and audit events
- Rollout, rollback, migration, and feature flag risks

For frontend profiles, review the implementation for:

- User workflow correctness
- Accessibility and keyboard operation
- Loading, empty, error, and success states
- Form validation and duplicate submission handling
- XSS and sensitive data exposure
- API error handling and user-safe messages
- Responsive layout and text wrapping
- Frontend tests for user behavior and failure paths

Use the severity rubric in the selected profile rule files.

## Step 4: Produce Review Report

Create a versioned Markdown review report unless the user explicitly passes
`--no-report-file`.

If a feature directory exists, write:

```text
<feature-directory>/reviews/review-vNNN-YYYYMMDD-HHMMSS.md
```

Also update:

```text
<feature-directory>/review.md
<feature-directory>/reviews/review-index.md
```

Otherwise write:

```text
.specify/reviews/review-vNNN-YYYYMMDD-HHMMSS.md
.specify/reviews/latest-review.md
.specify/reviews/review-index.md
```

Use `report.timestamp_format`, `report.directory_name`, `report.latest_alias`,
and `report.update_index` from the config when present. If those settings are
missing, default to the paths above.

Versioning rules:

1. The first report in a review directory is `v001`.
2. Each new report increments the highest existing version in that same
   directory: `v002`, `v003`, and so on.
3. Never overwrite a versioned report file.
4. Use a timestamp in local time by default, formatted as `YYYYMMDD-HHMMSS`.
5. Include an ISO-style generated-at timestamp in the report metadata.
6. Update `review.md` or `latest-review.md` as a pointer to the newest report.
7. Append the new report to `review-index.md` if `report.update_index` is true.

If available, use the helper script to allocate the report path before writing
the final content:

```powershell
.specify\extensions\review-profiles\scripts\powershell\new-review-report.ps1 `
  -FeatureDirectory <feature-directory> `
  -Profile <profile> `
  -Result <PASS | PASS WITH WARNINGS | NEEDS WORK> `
  -BaseBranch <base-branch> `
  -ReportDirectoryName <report.directory_name> `
  -LatestAlias <report.latest_alias>
```

If the helper script is not available, create the same directory and filename
structure manually.

Use this structure:

```markdown
# Review Report

## Report Metadata

- Report version: <v001 | v002 | ...>
- Generated at: <YYYY-MM-DDTHH:mm:ss+/-HH:mm>
- Timestamp: <YYYYMMDD-HHMMSS>
- Profile: <api | frontend | security | fullstack | custom>
- Result: PASS | PASS WITH WARNINGS | NEEDS WORK
- Feature directory: <path or "not found">
- Base branch: <branch used for diff>
- Head commit: <commit sha if available>
- Report file: <path>

## Review Result

- Profile: <api | frontend | security | fullstack | custom>
- Result: PASS | PASS WITH WARNINGS | NEEDS WORK
- Blockers: <count>
- Major issues: <count>
- Minor issues: <count>
- Info notes: <count>

## Executive Summary

Short summary for manager, QA, and dev lead.

## Changed Files Reviewed

List changed API, schema, test, documentation, and migration files.

## Rules Loaded

List the profile rule files used for this review.

## Findings

### BLOCKER-001: <title>

- Evidence: `<file>:<line>` and short explanation
- Why it matters: <impact>
- Suggested fix: <specific next step>
- Recommended owner: Dev | QA | Dev Lead | Security | Product

## Spec Traceability

| Spec/task item | Evidence found | Status | Notes |
| --- | --- | --- | --- |

## Test Coverage Review

| Required test area | Evidence found | Status | Notes |
| --- | --- | --- | --- |

## QA Notes

Concrete manual or automated QA checks to run.

## PR Comment Draft

A concise comment that can be pasted into the pull request.
```

## Step 5: Final Response

In the chat response, provide:

1. The review result
2. Counts by severity
3. The version and path to the written report
4. The top three issues to fix first
5. Any assumptions caused by missing spec or diff context

Keep the response concise and decision-oriented.