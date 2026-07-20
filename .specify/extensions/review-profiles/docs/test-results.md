# Test Results

## 2026-06-04

Validation performed locally on Windows with GitHub Spec Kit's Copilot
integration.

Command used:

```powershell
.\scripts\powershell\test-extension.ps1 -SpecKitProject "C:\path\to\spec-kit"
```

### Checks

- Manifest loaded successfully with Spec Kit's `ExtensionManifest`.
- Extension installed successfully into a fresh temporary Spec Kit project.
- Copilot agent command was generated:
  - `.github/agents/speckit.review-profiles.review.agent.md`
- Copilot alias command was generated:
  - `.github/agents/speckit.review-profiles.gate.agent.md`
- Copilot prompt files were generated:
  - `.github/prompts/speckit.review-profiles.review.prompt.md`
  - `.github/prompts/speckit.review-profiles.gate.prompt.md`
- Extension config and rules were copied into:
  - `.specify/extensions/review-profiles/`
- Profile rules are available for API, frontend, security, and fullstack review.

### Re-run

From the extension repo:

```powershell
.\scripts\powershell\test-extension.ps1 -SpecKitProject "C:\path\to\spec-kit"
```

Or, when `specify` is installed on `PATH`:

```powershell
.\scripts\powershell\test-extension.ps1
```

## 2026-06-10

Validation performed locally on Windows for versioned review report generation.

Command shape tested:

```powershell
.\scripts\powershell\new-review-report.ps1 `
  -FeatureDirectory "specs\001-demo" `
  -Profile api `
  -Result "NEEDS WORK" `
  -BaseBranch main
```

### Checks

- First run created `review-v001-YYYYMMDD-HHMMSS.md`.
- Second run created `review-v002-YYYYMMDD-HHMMSS.md`.
- `specs/<feature>/review.md` was updated as the latest-report pointer.
- `specs/<feature>/reviews/review-index.md` was created and appended.
- Full extension install smoke test passed against current GitHub Spec Kit.
- Spec Kit generated the Copilot agent and prompt files for the review command.
- Spec Kit copied the versioned report helper into
  `.specify/extensions/review-profiles/scripts/powershell/`.
