# Changelog

## 0.3.0

- Adds versioned review report output with timestamped filenames.
- Adds a PowerShell helper for allocating the next report version.
- Updates the review command to maintain a latest-report pointer and report index.
- Documents where report history is saved.

## 0.2.0

- Adds configurable review profiles for API, frontend, security, and fullstack review.
- Adds frontend and security/code-quality rule files.
- Updates the review command to support `--profile <name>`.
- Documents how to create organization-specific review profiles.

## 0.1.2

- Adds first-time-user usage guide for developers testing the extension.
- Links the usage guide from the README.

## 0.1.1

- Adds local validation script and sample example review report.
- Documents local test results and demo output.

## 0.1.0

- Initial extension release.
- Adds `/speckit.review-profiles.review`.
- Adds profile-based review rules and configuration.
- Writes a structured review report for manager, QA, and dev-lead review.
