# Customizing Review Profiles

The Spec Kit Review Profiles extension supports different review profiles so teams can
use different instructions for different types of work.

Examples:

```text
/speckit.review-profiles.review --profile api --base main --report-only
/speckit.review-profiles.review --profile frontend --base main --report-only
/speckit.review-profiles.review --profile security --base main --report-only
/speckit.review-profiles.review --profile fullstack --base main --report-only
```

## Why Profiles Exist

API work and frontend work should not be reviewed with the exact same checklist.

For API changes, reviewers usually care about:

- authentication and authorization
- backward compatibility
- OpenAPI/schema updates
- request validation
- error contracts
- contract tests
- rollout and rollback

For frontend changes, reviewers usually care about:

- user workflow correctness
- accessibility
- loading, empty, error, and success states
- form validation
- XSS and sensitive data exposure
- visual responsiveness
- UI tests

For security and code-quality review, reviewers usually care about:

- vulnerabilities
- secrets
- injection risks
- missing negative tests
- code smells
- maintainability
- reliability

## Where Profiles Are Configured

After installation, project-specific config lives here:

```text
.specify/extensions/review-profiles/review-profiles-config.yml
```

Profile rule files live here:

```text
.specify/extensions/review-profiles/rules/
```

Default rule files included by this extension:

```text
rules/api-code-review-rules.md
rules/frontend-code-review-rules.md
rules/security-code-quality-rules.md
```

## Profile Config Example

```yaml
review:
  default_profile: "api"

profiles:
  api:
    description: "API contract, compatibility, security, and tests"
    rule_files:
      - "rules/api-code-review-rules.md"
      - "rules/security-code-quality-rules.md"
  frontend:
    description: "Frontend workflow, accessibility, state, security, and tests"
    rule_files:
      - "rules/frontend-code-review-rules.md"
      - "rules/security-code-quality-rules.md"
  security:
    description: "Security vulnerabilities, tests, and code smells"
    rule_files:
      - "rules/security-code-quality-rules.md"
  fullstack:
    description: "API plus frontend plus security review"
    rule_files:
      - "rules/api-code-review-rules.md"
      - "rules/frontend-code-review-rules.md"
      - "rules/security-code-quality-rules.md"
```

## Choosing A Profile

Use `--profile` in the command:

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

If no profile is provided, the command uses:

```yaml
review:
  default_profile: "api"
```

For mixed changes:

```text
/speckit.review-profiles.review --profile fullstack --base main --report-only
```

## Creating A Client-Specific Profile

Create a new rule file:

```text
.specify/extensions/review-profiles/rules/org-api-rules.md
```

Then add it to config:

```yaml
profiles:
  org-api:
    description: "Client-specific API standards"
    rule_files:
      - "rules/api-code-review-rules.md"
      - "rules/security-code-quality-rules.md"
      - "rules/org-api-rules.md"
```

Run it:

```text
/speckit.review-profiles.review --profile org-api --base main --report-only
```

## Suggested Client API Rules

Add rules such as:

- All public API changes must update OpenAPI.
- All new endpoints must have authentication and authorization tests.
- All collection endpoints must define pagination or explicit limits.
- Error responses must use the approved `{ error: { code, message, requestId } }`
  shape.
- No API response may include PII unless the spec explicitly allows it.
- All write endpoints must define idempotency or retry behavior.

## Suggested Client Frontend Rules

Add rules such as:

- All forms require validation, disabled submit state, and visible field errors.
- All user-facing changes require loading, empty, error, and success states.
- All interactive controls must be keyboard accessible.
- No user-provided content may be rendered as HTML unless sanitized with an
  approved helper.
- All critical workflows need user-level tests.
- UI must work at mobile and desktop viewport sizes.

## Suggested Security And Code-Smell Rules

Add rules such as:

- No secrets or tokens in source, logs, errors, or tests.
- No raw SQL or shell execution using user-controlled input.
- No new dependency without clear justification.
- No duplicated business logic across layers.
- No security-sensitive behavior without negative tests.
- No broad exception swallowing without logging and user-safe recovery.
