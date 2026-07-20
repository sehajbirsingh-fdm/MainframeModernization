# Spec Kit Review Profiles

This Spec Kit extension adds profile-based automated review after implementation:

```text
/speckit.review-profiles.review
```

It is designed for teams using GitHub Copilot or another AI coding assistant to
make pull requests easier for managers, QA leads, and dev leads to review. The
review can use different profiles for API, frontend, security, fullstack, or
organization-specific rules.

## What It Does

After a developer implements a code change, the extension asks the coding agent
to review the code against:

- the feature spec, plan, and task list
- the project constitution
- organization review rules
- selected review profile rules
- changed source files and tests
- repository Copilot instructions and PR templates, if present

It then writes a review report, usually at:

```text
specs/<feature>/reviews/review-v001-YYYYMMDD-HHMMSS.md
```

It also updates:

```text
specs/<feature>/review.md
specs/<feature>/reviews/review-index.md
```

If no Spec Kit feature folder exists, it can still review the git diff and write
to:

```text
.specify/reviews/
```

Each run creates a new versioned report (`v001`, `v002`, `v003`) with a
timestamp, so review history is preserved instead of overwritten.

## Why This Is Useful

The goal is not to replace human review. The goal is to give human reviewers a
clean first-pass package:

- what changed
- whether the change matches the spec
- which selected review rules failed
- which tests are missing
- what QA should verify
- what issues are blockers versus minor cleanup

## Demo Workflow

For detailed first-time setup and testing instructions, see
[`docs/usage-guide.md`](docs/usage-guide.md).

In a project that already uses Spec Kit:

```bash
specify extension add --dev ../spec-kit-review-profiles
```

Then, after implementation:

```text
/speckit.review-profiles.review
```

For a repo that does not fully use SDD yet, you can still run it as a code review
demo against the current branch:

```text
/speckit.review-profiles.review --base main --report-only
```

Choose a specific review profile:

```text
/speckit.review-profiles.review --profile api --base main --report-only
/speckit.review-profiles.review --profile frontend --base main --report-only
/speckit.review-profiles.review --profile security --base main --report-only
/speckit.review-profiles.review --profile fullstack --base main --report-only
```

For a visual example of the output, see
[`docs/sample-review-profiles-report.md`](docs/sample-review-profiles-report.md).

## GitHub Copilot Notes

For Copilot projects, Spec Kit registers this extension as an agent command under:

```text
.github/agents/speckit.review-profiles.review.agent.md
```

It also creates a companion prompt file under:

```text
.github/prompts/speckit.review-profiles.review.prompt.md
```

## Review Command

Primary command:

```text
/speckit.review-profiles.review
```

Alias:

```text
/speckit.review-profiles.gate
```

## Configuration

Default review rules live in:

```text
review-profiles-config.yml
rules/api-code-review-rules.md
rules/frontend-code-review-rules.md
rules/security-code-quality-rules.md
scripts/powershell/new-review-report.ps1
```

When installed into a project, customize:

```text
.specify/extensions/review-profiles/review-profiles-config.yml
```

Good organization-specific additions:

- API versioning policy
- approved error response format
- required test types
- security checklist
- logging and PII rules
- performance rules for list/search endpoints
- rollout and rollback requirements
- report naming, retention, and indexing rules

For profile customization, see
[`docs/customizing-review-profiles.md`](docs/customizing-review-profiles.md).

## Local Validation

Run the extension self-test from this repo:

```powershell
.\scripts\powershell\test-extension.ps1
```

If `specify` is not installed on `PATH`, point the test at a local Spec Kit
checkout:

```powershell
.\scripts\powershell\test-extension.ps1 -SpecKitProject "C:\path\to\spec-kit"
```

The test creates a temporary Spec Kit project, installs this extension in dev
mode, and verifies that the Copilot agent and prompt files are generated.

Latest local test notes are in [`docs/test-results.md`](docs/test-results.md).

## Suggested Pitch

Use this language in the demo:

> We are not asking Copilot to approve its own work. We are using it as a first
> pass reviewer that checks the PR against your API standards, test expectations,
> and implementation plan. Human reviewers still make the final decision, but
> they start with a structured review package instead of a raw code diff.

## Publishing And Installation

Recommended setup:

1. Create a GitHub repo named `spec-kit-review-profiles`.
2. Push this folder to that repo.
3. Clone the repo beside any product repo where you want to test it.
4. Install locally with:

```bash
specify extension add --dev ../spec-kit-review-profiles
```

For later rollout, create a tagged release and install from an approved internal
catalog or direct archive URL.
