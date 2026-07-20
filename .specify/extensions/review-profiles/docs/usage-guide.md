# Spec Kit Review Profiles Usage Guide

This guide is for a developer testing the Spec Kit Review Profiles extension for the
first time. It assumes the developer may also be new to GitHub Spec Kit.

## What You Are Testing

The extension adds this command to a Spec Kit project:

```text
/speckit.review-profiles.review
```

The command asks GitHub Copilot or another supported coding assistant to perform
a structured first-pass review of API changes. It checks the code against API
review rules such as authentication, authorization, backward compatibility,
OpenAPI/schema updates, validation, error contracts, tests, observability, and
rollout risk.

The same command can also use different review profiles:

```text
/speckit.review-profiles.review --profile api
/speckit.review-profiles.review --profile frontend
/speckit.review-profiles.review --profile security
/speckit.review-profiles.review --profile fullstack
```

This lets API, frontend, security, and organization-specific reviews use different
instructions.

The goal is not to replace human review. The goal is to give QA, managers, and
dev leads a cleaner review package before they inspect the pull request.

## Prerequisites

Install or confirm these tools:

- Git
- VS Code
- GitHub Copilot extension for VS Code
- Access to the extension repo:
  `https://github.com/Mayank1619/spec-kit-review-profiles`
- `uv`, the Python tool runner used to install Spec Kit:
  `https://docs.astral.sh/uv/getting-started/installation/`

On Windows, run commands in PowerShell.

## Step 1: Install GitHub Spec Kit

Install the Spec Kit CLI:

```powershell
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
```

Verify it works:

```powershell
specify --version
```

If PowerShell cannot find `specify`, close and reopen the terminal, then try
again.

## Step 2: Clone The Review Extension

Choose a parent folder where you keep test projects, then run:

```powershell
git clone https://github.com/Mayank1619/spec-kit-review-profiles.git
```

You should now have:

```text
spec-kit-review-profiles/
```

## Step 3: Create A Fresh Spec Kit Demo Project

From the same parent folder, create a small test project:

```powershell
mkdir speckit-review-profiles-demo
cd speckit-review-profiles-demo
specify init --here --integration copilot --script ps --ignore-agent-tools
```

This initializes a Spec Kit project and creates Copilot command files.

## Step 4: Install The Review Profiles Extension

From inside `speckit-review-profiles-demo`, install the extension in local dev mode:

```powershell
specify extension add --dev ..\spec-kit-review-profiles
```

Verify it installed:

```powershell
specify extension list
```

Expected result:

```text
Spec Kit Review Profiles
Commands: 1
Status: Enabled
```

## Step 5: Confirm Copilot Command Files Exist

Check that these files were generated:

```text
.github/agents/speckit.review-profiles.review.agent.md
.github/agents/speckit.review-profiles.gate.agent.md
.github/prompts/speckit.review-profiles.review.prompt.md
.github/prompts/speckit.review-profiles.gate.prompt.md
```

If these files exist, Spec Kit successfully registered the command for Copilot.

## Step 6: Open The Project In VS Code

```powershell
code .
```

Open GitHub Copilot Chat in VS Code.

## Step 7: Run A Simple Review Command

In Copilot Chat, run:

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

If the demo repo has no API code yet, the command should still explain that no
meaningful API diff or Spec Kit feature artifacts were found. That is okay for
an installation smoke test.

## Step 8: Run A Better Demo With A Small API Change

For a stronger demo, create or use a small API project. Then intentionally make
an imperfect API change, such as:

```text
Add a new customer summary endpoint. Intentionally skip the OpenAPI update and
leave out authorization failure tests so we can test the review gate.
```

After the change, run:

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

Expected output:

- Review result: `PASS`, `PASS WITH WARNINGS`, or `NEEDS WORK`
- Counts for blocker, major, minor, and info findings
- Findings with evidence and suggested fixes
- Missing test coverage notes
- QA notes
- A PR comment draft
- A versioned Markdown review report, usually under
  `specs/<feature>/reviews/review-v001-YYYYMMDD-HHMMSS.md` or `.specify/reviews/`
- A latest-report pointer at `specs/<feature>/review.md`
- A report history index at `specs/<feature>/reviews/review-index.md`

See `docs/sample-review-profiles-report.md` in the extension repo for an example.

## Optional: Use Full Spec Kit Workflow

If you want to test the full spec-driven workflow, use this sequence in Copilot
Chat:

```text
/speckit.constitution Create principles focused on API compatibility, security, testing, and observability.
```

Then:

```text
/speckit.specify Add a customer summary API endpoint that returns customer ID, display name, account status, and risk score. It must require authorization and preserve existing customer API behavior.
```

Then:

```text
/speckit.plan Use the existing API framework and test stack. Update API schema documentation and add negative-path tests for auth and validation.
```

Then:

```text
/speckit.tasks
```

Then:

```text
/speckit.implement
```

Finally:

```text
/speckit.review-profiles.review --profile api --report-only
```

This tests the intended end-to-end flow:

```text
spec -> plan -> tasks -> implement -> API review
```

## Where Review Reports Are Saved

The extension keeps a history of review reports.

With a Spec Kit feature folder:

```text
specs/<feature>/reviews/review-v001-YYYYMMDD-HHMMSS.md
specs/<feature>/reviews/review-v002-YYYYMMDD-HHMMSS.md
specs/<feature>/review.md
specs/<feature>/reviews/review-index.md
```

Without a Spec Kit feature folder:

```text
.specify/reviews/review-v001-YYYYMMDD-HHMMSS.md
.specify/reviews/latest-review.md
.specify/reviews/review-index.md
```

`review.md` or `latest-review.md` points to the newest report. The versioned
files preserve the review history for QA, dev leads, managers, and PR evidence.

## Optional: Run The Extension Self-Test

The extension includes a local validation script. From inside
`spec-kit-review-profiles`, run:

```powershell
.\scripts\powershell\test-extension.ps1
```

This creates a temporary Spec Kit project, installs the extension, and verifies
that the Copilot command and prompt files are generated.

If `specify` is not available on `PATH`, but you have a local Spec Kit checkout:

```powershell
.\scripts\powershell\test-extension.ps1 -SpecKitProject "C:\path\to\spec-kit"
```

## How To Test In An Existing API Repo

From the root of an existing API repo:

```powershell
specify init --here --integration copilot --script ps --ignore-agent-tools
specify extension add --dev ..\spec-kit-review-profiles
```

Then create a feature branch and make an API change.

In Copilot Chat:

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

For existing repos, the extension can still be useful even without full Spec Kit
feature docs. It will review the git diff and use the organization API rules.

## Review Profiles And Rules

The extension supports multiple review profiles:

```text
api
frontend
security
fullstack
```

Example commands:

```text
/speckit.review-profiles.review --profile api --base main --report-only
/speckit.review-profiles.review --profile frontend --base main --report-only
/speckit.review-profiles.review --profile security --base main --report-only
/speckit.review-profiles.review --profile fullstack --base main --report-only
```

The main rules are here:

```text
.specify/extensions/review-profiles/rules/api-code-review-rules.md
.specify/extensions/review-profiles/rules/frontend-code-review-rules.md
.specify/extensions/review-profiles/rules/security-code-quality-rules.md
```

Project-specific config is here:

```text
.specify/extensions/review-profiles/review-profiles-config.yml
```

Customize these for the organization before a real team pilot.

Recommended organization-specific rules:

- API versioning policy
- Approved error response format
- Required OpenAPI/schema update policy
- Required test types
- Security and PII handling rules
- Logging and observability requirements
- Rollout and rollback expectations

For deeper customization, see:

```text
docs/customizing-review-profiles.md
```

## What To Send Back After Testing

Ask the developer to send:

- The command they ran
- The generated versioned review report
- The `review-index.md` entry if more than one review was run
- Whether Copilot created the report where expected
- Any findings that were useful
- Any findings that were noisy or wrong
- Whether the review would save time for QA, manager, or dev-lead review

## Troubleshooting

### `specify` is not recognized

Close and reopen the terminal after installing Spec Kit.

Then try:

```powershell
specify --version
```

### Extension install fails

Make sure you are running the command from inside the Spec Kit project:

```powershell
specify extension add --dev ..\spec-kit-review-profiles
```

Also confirm the extension folder exists at that relative path.

### Copilot command does not appear

Check these files exist:

```text
.github/agents/speckit.review-profiles.review.agent.md
.github/prompts/speckit.review-profiles.review.prompt.md
```

Then restart VS Code and reopen Copilot Chat.

### Review report is too generic

Add more project-specific rules to:

```text
.specify/extensions/review-profiles/review-profiles-config.yml
.specify/extensions/review-profiles/rules/api-code-review-rules.md
```

The better the rules, the better the review.

### No findings are produced

Make sure there is an actual code diff against the base branch:

```powershell
git status
git diff main...HEAD
```

If the repo uses `master`, `develop`, or another base branch, run:

```text
/speckit.review-profiles.review --profile api --base develop --report-only
```
