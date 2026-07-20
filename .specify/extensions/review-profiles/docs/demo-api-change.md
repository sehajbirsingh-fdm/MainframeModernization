# Demo: Spec Kit Review Profiles

This is a simple way to demonstrate the extension to a team without requiring
full spec-driven adoption on day one.

## Demo Story

Pretend the team is updating an existing Customer API.

Example change:

- Add `GET /api/customers/{id}/summary`
- Add `riskScore` to the response
- Update service logic and tests

## Demo Setup

1. Use a small API repo or sample branch.
2. Make or generate a realistic API change with Copilot.
3. Intentionally leave one or two review issues, such as:
   - no authorization check
   - missing validation failure test
   - missing OpenAPI update
   - inconsistent error response
4. Run:

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

## What To Show

Show that the review report gives the human reviewer:

- a summary of changed files
- blocker and major findings
- missing tests
- QA notes
- PR comment draft
- evidence tied to files and spec/tasks when available

## Recommended Talking Track

Say:

> This does not replace your dev lead, QA, or manager. It gives them a first-pass
> review that checks the code against rules your organization defines. The human
> reviewer still owns approval, but they spend less time discovering obvious
> misses and more time on design and business risk.

## SDD-Light Option

If the team is unsure about full SDD, start with this lighter workflow:

1. Ticket or issue describes the API change.
2. Developer uses Copilot to implement.
3. Developer runs `/speckit.review-profiles.review --profile api`.
4. PR includes the generated review report.
5. Human reviewer checks the findings and makes the final call.

Later, if the team likes it, add full Spec Kit artifacts:

```text
spec -> plan -> tasks -> implement -> api review
```
