# Presentation Notes

## Problem

AI-assisted coding makes developers faster, but it can make reviewers slower
because the reviewer has to reconstruct intent from a larger or messier diff.

## Proposed Fix

Add an automated review gate after implementation.

The review gate checks API changes against organization rules before the pull
request reaches the manager, QA lead, or dev lead.

## Why Spec Kit

Spec Kit already gives structure around implementation. This extension adds the
missing review package:

```text
implement -> review
```

It can also work in a lighter mode when there is no full spec yet.

## Demo Command

```text
/speckit.review-profiles.review --profile api --base main --report-only
```

For frontend changes:

```text
/speckit.review-profiles.review --profile frontend --base main --report-only
```

## What The Client Gets

- Consistent review criteria
- Earlier detection of missing tests and API contract issues
- Review reports that are easy for QA and leads to scan
- A path to stronger SDD later, without forcing it immediately
- Different review instructions for API, frontend, security, and fullstack work

## Boundaries

- The review does not approve the PR.
- The review does not replace CI, tests, security scanning, or human ownership.
- The review is most useful when the rules are specific to the organization.
